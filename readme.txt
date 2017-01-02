S E R V E R:
Um den Server zu starten, muss in der Konsole in den Programmieraufgaben-Ordner navigiert werden.
Dort muss dann das jar-Archiv (Server.jar) mit dem Befehl: java -jar Server.jar ausgefuehrt werden.
Daraufhin laeuft der Server mit der IP-Adresse des Computers und kommuniziert ueber die Port-Nummer 3141.
Wenn eine andere Port-Nummer verwendet werden soll, muss diese bei dem Befehl hintenangehaengt werden.

Bsp.: java -jar Server.jar 2345
Die Konsole zeigt anschlie�end alle Ausgaben des Servers.

Alternativ kann der Server durch �ffnen der beiliegende Server.jar Datei gestartet werden. 


C L I E N T:
Um den Client zu starten, muss in der Konsole in den Programmieraufgaben-Ordner navigiert werden.
Dort muss dann das jar-Archiv (Client.jar) mit dem Befehl: java -jar Client.jar ausgefuehrt werden.
Anschlie�end �ffnet sich ein Applikationsfenster, welches einen Client darstellt.

Um nun eine Verbindung zu einem Server aufzubauen, muss man die entsprechende IP-Adresse und
Port-Nummer in die zwei Textfelder vor dem Button "Verbinden" eingeben. 
Wenn dort nichts eingegeben wird, wird standardmae�ig der "localhost" mit der Port-Nummer "3141" verwendet.

Anschlie�end muss noch auf den Button "Verbinden" geklickt werden, damit der Client versucht, eine Verbindung
zum Server aufzunehmen.
Wenn die Verbindung hergestellt ist, kann man in das untere gro�e Textfeld die Nachrichten eingeben,
die zu dem Server geschickt werden sollen.
Diese Nachrichten m�ssen auf dem in der Programmieraufgabe vorgegebenen Protokoll basieren.

Alternativ kann der Client durch �ffnen der beiliegende Client.jar Datei gestartet werden.