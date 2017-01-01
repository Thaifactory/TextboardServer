Um den Server zu starten, muss in der Konsole in den Programmieraufgaben-Ordner navigiert werden.
Dort muss dann das jar-Archiv (Server.jar) mit dem Befehl: java Server.jar ausgeführt werden.
Daraufhin läuft der Server mit der IP-Adresse des Computers und kommuniziert über die Port-Nummer 3141.
Wenn eine andere Port-Nummer verwendet werden soll, muss diese bei dem Befehl hintenangehängt werden.
Bsp.: java Server.jar 2345
Die Konsole zeigt anschließend alle Ausgaben des Servers.

Um den Client zu starten, muss in der Konsole in den Programmieraufgaben-Ordner navigiert werden.
Dort muss dann das jar-Archiv (Client.jar) mit dem Befehl: java Client.jar ausgeführt werden.
Anschließend öffnet sich ein Applikationsfenster, welches einen Client darstellt.

Um nun eine Verbindung zu einem Server aufzubauen, muss man die entsprechende IP-Adresse und
Port-Nummer in die zwei Textfelder vor dem Button Verbinden eingeben.
Wenn dort nichts eingegeben wird, wird standardmäßig der localhost mit der Port-Nummer 3141 verwendet.
Anschließend muss noch auf den Button Verbinden geklickt werden, damit der Client versucht, eine Verbindung
zum Server aufzunehmen.
Wenn die Verbindung hergestellt ist, kann man in das untere große Textfeld die Nachrichten eingeben,
die zu dem Server geschickt werden sollen.
Diese Nachrichten müssen auf dem in der Programmieraufgabe vorgegebenen Protokoll basieren.