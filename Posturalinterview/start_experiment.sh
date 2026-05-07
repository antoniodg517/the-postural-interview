#!/bin/bash

# Postural Interview - Quick Start Script
# Questo script avvia l'esperimento

echo "=============================================="
echo "   THE POSTURAL INTERVIEW EXPERIMENT"
echo "=============================================="
echo ""
echo "CHECKLIST PRE-ESPERIMENTO:"
echo "□ Sistema EMG wireless acceso e calibrato"
echo "□ Elettrodi applicati su trapezio ed erettori spinali"
echo "□ Baseline EMG registrata (3-5 min a riposo)"
echo "□ Consenso informato firmato"
echo "□ ID partecipante pronto"
echo ""
read -p "Premi INVIO per continuare..."

# Build del progetto
echo ""
echo "Building progetto..."
./gradlew build --quiet

if [ $? -eq 0 ]; then
    echo "✓ Build completato"
    echo ""
    
    # Crea directory logs se non esiste
    mkdir -p logs
    
    # Avvia la skill
    echo "Avvio Postural Interview Skill..."
    echo "=============================================="
    ./gradlew run
else
    echo "✗ Errore durante il build"
    exit 1
fi
