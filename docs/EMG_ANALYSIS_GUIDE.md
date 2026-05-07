# Guida Analisi Dati EMG - The Postural Interview

## Preparazione Dati

### 1. Sincronizzazione EMG con Eventi Furhat

Il file CSV generato da Furhat contiene timestamp precisi per tutti gli eventi:
```
logs/postural_interview_P001_20260114_143022.csv
```

**Metodo A: Sincronizzazione Manuale**
1. Identifica l'evento `SESSION:START` nel log Furhat (timestamp: T_start)
2. Identifica l'inizio della registrazione EMG (timestamp: EMG_start)
3. Offset = T_start - EMG_start
4. Allinea tutti gli eventi Furhat con il segnale EMG usando l'offset

**Metodo B: Lab Streaming Layer (LSL)** - RACCOMANDATO
- Usa LSL per inviare marker temporali dall'app Furhat al sistema EMG in real-time
- Richiede integrazione LSL nel codice Furhat (EventLogger può essere esteso)

### 2. Filtraggio del Segnale EMG

**Pre-processing pipeline raccomandato:**

```python
import numpy as np
from scipy import signal

def preprocess_emg(raw_signal, fs=1000):
    """
    fs: sampling frequency in Hz (tipicamente 1000 Hz)
    """
    # 1. Rimozione DC offset
    emg = raw_signal - np.mean(raw_signal)
    
    # 2. Bandpass filter (20-450 Hz)
    # Rimuove motion artifacts (< 20 Hz) e high-frequency noise (> 450 Hz)
    nyq = 0.5 * fs
    low = 20 / nyq
    high = 450 / nyq
    b, a = signal.butter(4, [low, high], btype='band')
    emg_filtered = signal.filtfilt(b, a, emg)
    
    # 3. Notch filter per powerline (50 Hz o 60 Hz)
    f0 = 50.0  # 50 Hz per Europa, 60 Hz per USA
    Q = 30.0
    b, a = signal.iirnotch(f0, Q, fs)
    emg_clean = signal.filtfilt(b, a, emg_filtered)
    
    return emg_clean
```

## Feature Extraction

### Finestre Temporali di Interesse

```python
import pandas as pd

# Carica log eventi
events = pd.read_csv('logs/postural_interview_P001_20260114_143022.csv')

# Definisci finestre
windows = {
    'baseline_pre': {
        'start': events[events['event_name'] == 'PRE-INTERVIEW']['elapsed_ms'].values[0],
        'duration': 30000  # ultimi 30 secondi di baseline
    },
    'cognitive_load': {
        'start': events[
            (events['event_type'] == 'STRESSOR') & 
            (events['event_name'] == 'COGNITIVE_LOAD')
        ]['elapsed_ms'].values[0],
        'duration': 15000  # primi 15 secondi dello stressor
    },
    'competency_challenge': {
        'start': events[
            (events['event_type'] == 'STRESSOR') & 
            (events['event_name'] == 'COMPETENCY_CHALLENGE')
        ]['elapsed_ms'].values[0],
        'duration': 15000
    },
    'evaluative_silence': {
        'start': events[events['event_name'] == 'START']['elapsed_ms'].values[
            events[events['event_name'] == 'START'].index[-1]  # ultimo START = silence
        ],
        'duration': 7000  # durata del silenzio
    },
    'baseline_post': {
        'start': events[events['event_name'] == 'POST-INTERVIEW-END']['elapsed_ms'].values[0] - 30000,
        'duration': 30000  # ultimi 30 secondi di recovery
    }
}
```

### Feature EMG Standard

```python
def extract_emg_features(signal, fs=1000):
    """
    Estrae feature standard dal segnale EMG
    """
    # Rectify signal
    rectified = np.abs(signal)
    
    features = {}
    
    # 1. Root Mean Square (RMS) - misura di tensione muscolare
    features['rms'] = np.sqrt(np.mean(signal**2))
    
    # 2. Integrated EMG (iEMG) - attività muscolare totale
    features['iemg'] = np.sum(rectified)
    
    # 3. Mean Absolute Value (MAV)
    features['mav'] = np.mean(rectified)
    
    # 4. Waveform Length (WL) - complessità del segnale
    features['wl'] = np.sum(np.abs(np.diff(signal)))
    
    # 5. Zero Crossings (ZC) - frequenza del segnale
    features['zc'] = np.sum(np.diff(np.sign(signal)) != 0)
    
    # 6. Power Spectral Density
    freqs, psd = signal.welch(signal, fs=fs, nperseg=256)
    
    # Median Frequency
    cumsum_psd = np.cumsum(psd)
    median_idx = np.where(cumsum_psd >= cumsum_psd[-1] / 2)[0][0]
    features['median_freq'] = freqs[median_idx]
    
    # Mean Frequency
    features['mean_freq'] = np.sum(freqs * psd) / np.sum(psd)
    
    # 7. Peak Amplitude
    features['peak_amplitude'] = np.max(rectified)
    
    return features
```

### Analisi per Muscolo

```python
def analyze_muscle_tension(emg_data, events, muscle_name='trapezio_dx'):
    """
    Analizza la tensione muscolare attraverso le diverse fasi
    """
    results = {}
    
    for window_name, window_info in windows.items():
        start_idx = int(window_info['start'] * fs / 1000)
        end_idx = start_idx + int(window_info['duration'] * fs / 1000)
        
        window_signal = emg_data[start_idx:end_idx]
        window_signal = preprocess_emg(window_signal)
        
        results[window_name] = extract_emg_features(window_signal)
    
    return results
```

## Analisi Statistica

### Confronto Baseline vs. Stressor

```python
from scipy import stats

def compare_baseline_stressor(baseline_features, stressor_features):
    """
    Test statistico per confrontare baseline e stressor
    """
    # Usa RMS come misura principale di tensione
    baseline_rms = baseline_features['rms']
    stressor_rms = stressor_features['rms']
    
    # Calcola variazione percentuale
    percent_change = ((stressor_rms - baseline_rms) / baseline_rms) * 100
    
    # Effect size (Cohen's d)
    pooled_std = np.sqrt((np.var([baseline_rms]) + np.var([stressor_rms])) / 2)
    cohens_d = (stressor_rms - baseline_rms) / pooled_std
    
    return {
        'baseline_rms': baseline_rms,
        'stressor_rms': stressor_rms,
        'percent_change': percent_change,
        'cohens_d': cohens_d
    }
```

### Tempo di Recupero

```python
def calculate_recovery_time(recovery_signal, baseline_rms, fs=1000, threshold=1.1):
    """
    Calcola il tempo necessario per tornare al livello baseline
    threshold: 1.1 = entro 10% del baseline
    """
    # Smooth signal con moving average
    window_size = int(1 * fs)  # 1 secondo
    smoothed = np.convolve(recovery_signal, 
                          np.ones(window_size)/window_size, 
                          mode='valid')
    
    # Calcola RMS in finestre di 1 secondo
    rms_values = []
    for i in range(0, len(smoothed), fs):
        window = smoothed[i:i+fs]
        rms_values.append(np.sqrt(np.mean(window**2)))
    
    # Trova primo punto sotto threshold
    target_rms = baseline_rms * threshold
    recovery_idx = np.where(np.array(rms_values) <= target_rms)[0]
    
    if len(recovery_idx) > 0:
        recovery_time = recovery_idx[0]  # in secondi
        return recovery_time
    else:
        return None  # Non recuperato completamente
```

## Visualizzazione

```python
import matplotlib.pyplot as plt

def plot_emg_timeline(emg_data, events, muscle_name='Trapezio DX', fs=1000):
    """
    Plot del segnale EMG con evidenziazione degli eventi
    """
    time = np.arange(len(emg_data)) / fs / 1000  # in secondi
    
    fig, ax = plt.subplots(figsize=(15, 5))
    ax.plot(time, emg_data, linewidth=0.5, alpha=0.7)
    
    # Evidenzia stressor
    stressor_events = events[events['event_type'] == 'STRESSOR']
    for _, event in stressor_events.iterrows():
        t_start = event['elapsed_ms'] / 1000
        ax.axvline(t_start, color='red', linestyle='--', alpha=0.5)
        ax.text(t_start, ax.get_ylim()[1], 
                event['event_name'], 
                rotation=90, 
                verticalalignment='top')
    
    # Evidenzia silenzi
    silence_starts = events[
        (events['event_type'] == 'SILENCE') & 
        (events['event_name'] == 'START')
    ]
    for _, event in silence_starts.iterrows():
        t_start = event['elapsed_ms'] / 1000
        duration = float(event['details'].split(':')[1].strip().split()[0])
        ax.axvspan(t_start, t_start + duration, 
                   alpha=0.3, 
                   color='yellow', 
                   label='Silenzio Valutativo')
    
    ax.set_xlabel('Tempo (s)')
    ax.set_ylabel('Ampiezza EMG (μV)')
    ax.set_title(f'Segnale EMG - {muscle_name}')
    ax.legend()
    ax.grid(True, alpha=0.3)
    
    return fig

def plot_feature_comparison(results_dict, feature='rms', muscle='trapezio_dx'):
    """
    Confronto grafico delle feature tra le diverse fasi
    """
    phases = list(results_dict.keys())
    values = [results_dict[phase][feature] for phase in phases]
    
    fig, ax = plt.subplots(figsize=(10, 6))
    colors = ['green', 'orange', 'red', 'red', 'blue']
    bars = ax.bar(phases, values, color=colors, alpha=0.7)
    
    # Linea baseline
    baseline_value = results_dict['baseline_pre'][feature]
    ax.axhline(baseline_value, color='green', linestyle='--', 
               linewidth=2, label='Baseline')
    
    ax.set_ylabel(f'{feature.upper()} Value')
    ax.set_title(f'{feature.upper()} Comparison - {muscle}')
    ax.legend()
    ax.grid(True, axis='y', alpha=0.3)
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    
    return fig
```

## Analisi Multi-Soggetto

```python
def aggregate_results(participant_ids):
    """
    Aggrega risultati da più partecipanti
    """
    all_results = []
    
    for pid in participant_ids:
        # Carica dati per questo partecipante
        emg_file = f'data/emg_{pid}.csv'
        log_file = f'logs/postural_interview_{pid}_*.csv'
        
        # Analizza
        results = analyze_muscle_tension(emg_data, events)
        results['participant_id'] = pid
        all_results.append(results)
    
    # Converti in DataFrame per analisi
    df = pd.DataFrame(all_results)
    
    # Statistiche di gruppo
    group_stats = df.groupby('phase').agg({
        'rms': ['mean', 'std', 'sem'],
        'mav': ['mean', 'std', 'sem']
    })
    
    return df, group_stats
```

## Report Finale

```python
def generate_report(results, participant_id):
    """
    Genera report completo per un partecipante
    """
    report = f"""
    ====================================
    POSTURAL INTERVIEW - REPORT EMG
    Partecipante: {participant_id}
    ====================================
    
    TENSIONE MUSCOLARE (RMS):
    """
    
    for phase, features in results.items():
        report += f"\n{phase:25s}: {features['rms']:.2f} μV"
    
    # Variazioni rispetto a baseline
    baseline = results['baseline_pre']['rms']
    report += "\n\nVARIAZIONI % vs BASELINE:\n"
    
    for phase in ['cognitive_load', 'competency_challenge', 'evaluative_silence']:
        change = ((results[phase]['rms'] - baseline) / baseline) * 100
        report += f"{phase:25s}: {change:+.1f}%\n"
    
    # Recovery
    recovery = ((results['baseline_post']['rms'] - baseline) / baseline) * 100
    report += f"\nRecupero post-stress: {recovery:+.1f}%"
    
    print(report)
    
    # Salva report
    with open(f'reports/report_{participant_id}.txt', 'w') as f:
        f.write(report)
```

## Esempio Completo

```python
# Script di analisi completo
import numpy as np
import pandas as pd

# 1. Carica dati
participant_id = 'P001'
emg_data = np.loadtxt(f'data/emg_{participant_id}.csv', delimiter=',')
events = pd.read_csv(f'logs/postural_interview_{participant_id}_20260114_143022.csv')

# 2. Preprocessing
emg_clean = preprocess_emg(emg_data[:, 1])  # Trapezio DX (colonna 1)

# 3. Estrai feature
results = analyze_muscle_tension(emg_clean, events, 'trapezio_dx')

# 4. Confronti statistici
comparisons = {}
for stressor in ['cognitive_load', 'competency_challenge', 'evaluative_silence']:
    comparisons[stressor] = compare_baseline_stressor(
        results['baseline_pre'], 
        results[stressor]
    )

# 5. Visualizza
fig1 = plot_emg_timeline(emg_clean, events, 'Trapezio DX')
fig2 = plot_feature_comparison(results, 'rms', 'trapezio_dx')

# 6. Genera report
generate_report(results, participant_id)

# 7. Salva figure
fig1.savefig(f'figures/{participant_id}_timeline.png', dpi=300)
fig2.savefig(f'figures/{participant_id}_comparison.png', dpi=300)
```

## Metriche Chiave per Paper

1. **Reattività allo Stress**: % variazione RMS baseline→stressor
2. **Differenze tra Stressor**: ANOVA tra i 3 tipi di stressor
3. **Pattern Temporale**: Latenza del picco dopo onset stressor
4. **Tempo di Recupero**: Secondi per tornare a baseline ±10%
5. **Asimmetria**: Confronto lato DX vs SX
6. **Correlazione Soggettiva**: Stress percepito vs RMS misurato
