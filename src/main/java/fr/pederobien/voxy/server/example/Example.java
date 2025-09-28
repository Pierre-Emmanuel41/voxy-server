package fr.pederobien.voxy.server.example;

import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.impl.VoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class Example {

	public static void main(String[] args) {
		Logger.instance().newLine(true).timeStamp(true).colorized(true).debug(true);

		IVoxyServer server = new VoxyServer("Voxy_Server", 12345);
		server.open();

		try {
			Thread.sleep(200000000);
		} catch (InterruptedException e) {
			// Do nothing
		}

		server.close();
		server.dispose();
	}
}
