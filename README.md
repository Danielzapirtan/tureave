# Planificator ture și concedii

O aplicație web statică pentru planificarea schimburilor de lucru și a concediilor, cu un calendar lună pe lună și statistici automate bazate pe turele ciclului și zilele libere legale.

## Ce face aplicația

- afișează un calendar lunar pentru zilele lucrătoare, weekend și sărbători legale;
- aplică automat un ciclu de ture (`zi` / `noapte` / `liber`);
- permite suprascrierea unei zile cu `lucrat`, `parțial lucrat`, `concediu` sau `liber`;
- calculează automat orele țintă, orele realizate și diferența față de target;
- păstrează preferințele local, în browser, prin `localStorage`;
- încarcă sărbătorile legale din API-uri publice cu fallback.

## Tehnologie

Proiectul este un singur fișier HTML, fără framework și fără proces de build:

- `index.html` — interfață, stiluri și logica aplicației

## Pornire rapidă

1. Deschide `index.html` direct din browser, sau
2. Rulează un server local din folderul proiectului:

```bash
python3 -m http.server 8000
```

Apoi accesează:

```text
http://localhost:8000
```

## Structura aplicației

- navigare lună/an
- grilă calendaristică cu zilele săptămânii și sărbători
- panou statistic cu ore țintă și diferență
- editor pentru statusul unei zile
- salvare locală a modificărilor

## Funcționalitate principală

Fiecare zi este evaluată astfel:

- ciclul implicit definește dacă ziua este de `zi`, `noapte` sau `liber`;
- dacă există o suprascriere în `localStorage`, aceasta are prioritate;
- dacă nu există suprascriere, se folosește statusul implicit din ciclu.

## Observații

- aplicația este proiectată pentru utilizare internă și pentru un flux simplu de planificare;
- datele legate de sărbători sunt încărcate din surse externe și pot fi disponibile doar dacă API-urile respective răspund;
- nu există backend sau autentificare, deoarece scopul este de a rula complet în browser.

## Licență

Acest repo nu include o licență definită în momentul de față. Dacă este nevoie de o licență explicită, aceasta trebuie adăugată înainte de distribuirea publică a proiectului.
