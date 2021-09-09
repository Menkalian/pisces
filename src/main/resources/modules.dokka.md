# Package de.menkalian.pisces

Dieses Package enthält die grundlegende `Spring-Boot`-Applikation, sowie einige übergreifende Schnittstellen. Der hier implementierte Code ist minimal gehalten und beschränkt sich auf die Initialisierung der Komponenten.

# Package de.menkalian.pisces.audio

Gekapselter Code zum Handling der Audiofunktionalitäten. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IAudioHandler][de.menkalian.pisces.audio.IAudioHandler]. Über diesen Handler werden [IGuildAudioController][de.menkalian.pisces.audio.IGuildAudioController]-Instanzen bereitgestellt, die Audiofunktionalitäten pro Discord-Server bereitstellen.

# Package de.menkalian.pisces.command

Gekapselter Code zur Behandlung der Kommandos. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [ICommandHandler][de.menkalian.pisces.command.ICommandHandler]. Dieser Handler verwaltet die [ICommand][de.menkalian.pisces.command.ICommand]-Instanzen und deren Ausführung.

# Package de.menkalian.pisces.config

Programmatische Konfiguration der Applikation. Dieses Package enthält kein [IHandler][de.menkalian.pisces.IHandler]-Interface. Die aktive Konfiguration kann über den [ConfigProvider][de.menkalian.pisces.config.ConfigProvider] oder eine Spring-Injection für eine [IConfig][de.menkalian.pisces.config.IConfig]-Instanz erhalten werden. Die eigentliche Einstellung der Konfiguration erfolgt in der `Default`-Implementierung der Unterkonfigurationen (z.B. [DefaultFeatureConfig][de.menkalian.pisces.config.DefaultFeatureConfig]).

# Package de.menkalian.pisces.database

Kapselung der Datenbankzugriffe. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDatabaseHandler][de.menkalian.pisces.database.IDatabaseHandler]. Aktuell unterstützt die Applikation die folgenden DBMS:

* HSQLDB über `spring-data-jpa`

# Package de.menkalian.pisces.discord

Kapselt die Anmeldung und grundsätzlichen Zugriff auf die Discord-API. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDiscordHandler][de.menkalian.pisces.discord.IDiscordHandler]. Da nicht die vollständige Discord-API neu abgebildet wird, ist die Hauptaufgabe dieser Schnittstelle die aktuelle Instanz des API-Handlers (aktuell `JDA`) bereitzustellen.

# Package de.menkalian.pisces.message

Kapselt das Senden und die Manipulation von Nachrichten und Reaktionen. Das [IHandler][de.menkalian.pisces.IHandler]-Interface der Komponente ist [IDiscordHandler][de.menkalian.pisces.message.IMessageHandler].

# Package de.menkalian.pisces.util

Hilfscode, der für mehrere Teile der Applikation relevant ist. Hierzu gehören Typaliase, Teilimplementierungen von Schnittstellen, die Definition von Erweiterungsmethoden, sowie klassische Hilfsmethoden.