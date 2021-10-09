# Package de.menkalian.pisces

Dieses Package enthält die grundlegende `Spring-Boot`-Applikation, sowie einige übergreifende Schnittstellen. Der hier implementierte Code ist minimal gehalten und beschränkt sich auf die Initialisierung der Komponenten.

# Package de.menkalian.pisces.audio

Gekapselter Code zum Handling der Audiofunktionalitäten. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IAudioHandler][de.menkalian.pisces.audio.IAudioHandler]. Über diesen Handler werden [IGuildAudioController][de.menkalian.pisces.audio.IGuildAudioController]-Instanzen bereitgestellt, die Audiofunktionalitäten pro Discord-Server bereitstellen.

# Package de.menkalian.pisces.audio.data

Datenobjekte für die Audiofunktionalitäten. Diese Objekte sind unabhängig von der verwendeten Audiobibliothek (aktuell Lavaplayer) und bieten daher eine Abstraktion, um andere Module unabhängig von Lavaplayer zu halten.

# Package de.menkalian.pisces.audio.sending

Enthält Klassen zur eigentlichen Implementierung des Sendevorgangs. Die Implementierung dieser Klassen können sich beispielsweise darin unterscheiden wie die Audiodaten verarbeitet werden oder woher diese bezogen werden.

# Package de.menkalian.pisces.audio.sending.filter

Platzhalter für eine zukünftige (experimentelle) Funktion zur Manipulation der Audiodaten durch Filter

# Package de.menkalian.pisces.command

Gekapselter Code zur Behandlung der Kommandos. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [ICommandHandler][de.menkalian.pisces.command.ICommandHandler]. Dieser Handler verwaltet die [ICommand][de.menkalian.pisces.command.ICommand]-Instanzen und deren Ausführung.

# Package de.menkalian.pisces.command.data

Datenstrukturen und Enums zur Befehlsbehandlung

# Package de.menkalian.pisces.command.impl.audio

Befehlsimplementierungen für Befehle, die direkt zur Audio-/Soundbehandlung verbunden sind

# Package de.menkalian.pisces.command.impl.audio.playback

Befehlsimplementierungen für Befehle, die zum Starten von Audiowiedergaben verwendet werden

# Package de.menkalian.pisces.command.impl.audio.playlist

Befehlsimplementierungen für Befehle, die mit Playlisten (oder deren Wiedergabe) zusammenhängen

# Package de.menkalian.pisces.command.impl.audio.queue

Befehlsimplementierungen für Befehle, die die aktuelle Wiedergabeliste betreffen

# Package de.menkalian.pisces.command.impl.audio.songcontrol

Befehlsimplementierungen für Befehle, die die aktuelle Wiedergabe beeinflussen

# Package de.menkalian.pisces.command.impl.base

Befehlsimplementierungen für grundlegende Befehle (z.B. `help`/`usage`)

# Package de.menkalian.pisces.command.impl.settings

Befehlsimplementierungen für Befehle, die Einstellungen verändern

# Package de.menkalian.pisces.command.impl.settings.joinsound

Befehlsimplementierungen für Einstellungsbefehle die Joinsounds betreffen

# Package de.menkalian.pisces.command.impl.settings.prefix

Befehlsimplementierungen für Einstellungsbefehle die das Befehlspräfix betreffen

# Package de.menkalian.pisces.command.impl.settings.repeat

Befehlsimplementierungen für Einstellungsbefehl, die das Wiederholungsverhalten betreffen

# Package de.menkalian.pisces.command.impl.settings.shuffle

Befehlsimplementierungen für Einstellungsbefehle die den Zufallsmix betreffen

# Package de.menkalian.pisces.command.listener

Implementierungen von JDA-Listenern

# Package de.menkalian.pisces.config

Programmatische Konfiguration der Applikation. Dieses Package enthält kein [IHandler][de.menkalian.pisces.IHandler]-Interface. Die aktive Konfiguration kann über den [ConfigProvider][de.menkalian.pisces.config.ConfigProvider] oder eine Spring-Injection für eine [IConfig][de.menkalian.pisces.config.IConfig]-Instanz erhalten werden. Die eigentliche Einstellung der Konfiguration erfolgt in der `Default`-Implementierung der Unterkonfigurationen (z.B. [DefaultFeatureConfig][de.menkalian.pisces.config.DefaultFeatureConfig]).

# Package de.menkalian.pisces.database

Kapselung der Datenbankzugriffe. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDatabaseHandler][de.menkalian.pisces.database.IDatabaseHandler]. Aktuell unterstützt die Applikation die folgenden DBMS:

* HSQLDB über `spring-data-jpa`
* MariaDB über `spring-data-jpa`

# Package de.menkalian.pisces.database.data

Datenklassen zur vereinfachten Repräsentation von Objekten, die in der Datenbank abgelegt werden (oder daraus gelesen wurden)

# Package de.menkalian.pisces.database.jpa

JPA-Objekte und Repository-Interfaces. Die Repository-Interfaces werden von Spring automatisch implementiert und als Bean zur Verfügung gestellt.

# Package de.menkalian.pisces.discord

Kapselt die Anmeldung und grundsätzlichen Zugriff auf die Discord-API. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDiscordHandler][de.menkalian.pisces.discord.IDiscordHandler]. Da nicht die vollständige Discord-API neu abgebildet wird, ist die Hauptaufgabe dieser Schnittstelle die aktuelle Instanz des API-Handlers (aktuell `JDA`) bereitzustellen.

# Package de.menkalian.pisces.message

Kapselt das Senden und die Manipulation von Nachrichten und Reaktionen. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDiscordHandler][de.menkalian.pisces.message.IMessageHandler].

# Package de.menkalian.pisces.message.spec

Datenklassen zur Beschreibung von Nachrichten. Das Ziel ist, dass die Commands nur den *Inhalt* der Nachrichten definieren, während sich die [Instanz der Nachricht][de.menkalian.pisces.message.IMessageInstance] um die *Darstellung* kümmert.

# Package de.menkalian.pisces.util

Hilfscode, der für mehrere Teile der Applikation relevant ist. Hierzu gehören Typaliase, Teilimplementierungen von Schnittstellen, die Definition von Erweiterungsmethoden, sowie klassische Hilfsmethoden.