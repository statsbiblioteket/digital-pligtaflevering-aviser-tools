Generel info validering
---
Denne vejledning er ment som en kort instruktion til slutbrugerne af manuel kontrol af aviser

Produktionsserveren ligger på stien "http://aldebaran:9021/dpa-manualcontrol"
Stageserveren ligger på stien "http://mirzam:9021/dpa-manualcontrol"

Det er Produktionsserveren der skal benyttes i det daglige arbejde, Stageserveren er kun til at validere funktionalitet.

Der skal logges ind med AD-bruger.


Påbegynd validering af en måneds leveringer
---

Når validering af en ny måned skal påbegyndes starter man med at vælge den ønskede måned i datovælgeren øverst til venstre.
Derefter vælges "Prepare month"

Efter klik på "Prepare month" går der noget tid hvor systemet casher den information der er gemt om de leverede aviser.
Når waitindikatoren er holdt op med at snurre kan man klikke på start, hvorefter aviserne der er leveret vises i brugerfladen.


Valider avisindhold
---

Klik på en avis og en delivery, herefter vises alle de sider der er leveret på den pågældende avis og dag.

Hver gang der klikkes på en side i tabellen "Page" vises denne side i browseren.
Hvis en side kan godkendes klikkes på "Confirmed" over avis-siden. Hvis in side ikke kan godkendes klikkes på "Reject" over avis-siden.

Når kontrol af en enhed bestående af en avistitel og en dato er færdiggjort klikkes på "Save check" øverst til venstre i browseren.
Der åbnes en dialog hvor der er målighed for at se hvilke informationer der er rigistreret om godkendte sider, og der er mulighed for at skrive en kommentar.
Når der klikkes "Ok" bliver informationen gemt, og systemet har derefter registreret at denne enhed er kontrolleret.


Ekstra info
---

Der er to væsentlige sider til brug i det daglige arbejde:
- Delivery validation (der vælges først delivery og derefter titel)
- TitleValidation (der vælges først titel og derefter delivery)

Der er mulighed for at starte digekte på TitleValidation med dette link
http://aldebaran:9021/dpa-manualcontrol/#!TITLEVALIDATIONPANEL

Der er mulighed for at starte digekte på Delivery validation med dette link
http://localhost:8080/dpa-manualcontrol/#!DELIVERYPANEL

Der er mulighed for at få et overblik over hvad der er leveret i en kaldendervisning på siden overview



Der er mulighed for at starte direkte på en bestemt måned ved at sætte en månedsparameter ind i url'en:
?month=201704 (for april 2017)


Hvis man vil have browseren til at starte på siden TitleValidation for april 2017 skal følgende url benyttes:
http://aldebaran:9021/dpa-manualcontrol/?month=201704#!TITLEVALIDATIONPANEL


Der er mulighed for at validere aviser der ikke er kørt igennem de automatiske kontrol-steps.
I sommeren 2018 sættes flere automatiske kontrol-steps i drift.
Hvis der er behov for at lave manuel kontrol på deliveries der ikke er kørt gennem alle automatiske steps kan dette gøres således:
?events=DONEMANUALMINIMALCHECK

Der kan benyttes flere samtidige konfigurationer. f.ex. kan man køre med april 2017 samtidigt med tidlig kontrol således:
http://aldebaran:9021/dpa-manualcontrol/?month=201704&events=DONEMANUALMINIMALCHECK#!TITLEVALIDATIONPANEL

Generelt bør "?events=DONEMANUALMINIMALCHECK" kun benyttes indtil de automatiske kontroller begynder at køre


