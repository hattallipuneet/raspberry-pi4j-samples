package util.tests;


import http.utils.DumpUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import java.net.SocketException;

import java.util.Date;

public class TCPClient {
	private int tcpport = 80;
	private String hostName = "localhost";

	private boolean goRead = true;

	public TCPClient() {
	}

	public TCPClient(int tcp) {
		tcpport = tcp;
	}

	public TCPClient(String host, int tcp) {
		hostName = host;
		tcpport = tcp;
	}

	private Socket skt = null;

	private boolean canRead() {
		return this.goRead;
	}

	public void read() {
		read(null);
	}

	public void read(String request) {
		boolean verbose = "true".equals((System.getProperty("verbose", "false")));
		System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpport + " on " + hostName);
		try {
			InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
			skt = new Socket(address, tcpport);

			if (request != null) {
				OutputStream os = skt.getOutputStream();
				DataOutputStream out = new DataOutputStream(os);
				out.writeBytes(request + "\n"); // LF is the end of message!!!
				out.flush();
			}

			InputStream theInput = skt.getInputStream();
			byte buffer[] = new byte[4096];
			String s;
			int nbReadTest = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (canRead()) {
				int bytesRead = theInput.read(buffer);
				if (bytesRead == -1) {
					System.out.println("Nothing to read...");
					if (nbReadTest++ > 10)
						break;
				} else {
					baos.write(buffer, 0, bytesRead);
					if (verbose) {
						System.out.println("# Read " + bytesRead + " characters");
						System.out.println("# " + (new Date()).toString());
					}
					if (buffer[bytesRead -1] == '\n') {
						String message = baos.toString().trim();
						DumpUtil.displayDualDump(message);
						// Manage message here

					}
				}
			}
			System.out.println("Stop Reading TCP port.");
			theInput.close();
		} catch (BindException be) {
			System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpport);
			be.printStackTrace();
			manageError(be);
		} catch (final SocketException se) {
//    se.printStackTrace();
			if (se.getMessage().indexOf("Connection refused") > -1) {
				System.err.println("Refused (1)");
				se.printStackTrace();
			} else if (se.getMessage().indexOf("Connection reset") > -1) {
				System.err.println("Reset (2)");
				se.printStackTrace();
			} else {
				boolean tryAgain = false;
				if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage()))
					tryAgain = true;
				else if (se instanceof ConnectException && "Network is unreachable: connect".equals(se.getMessage()))
					tryAgain = true;
				else if (se instanceof ConnectException) // Et hop!
				{
					tryAgain = false;
					System.err.println("TCP :" + se.getMessage());
					se.printStackTrace();
				} else {
					tryAgain = false;
					System.err.println("TCP Socket:" + se.getMessage());
					se.printStackTrace();
				}

				if (tryAgain) {
					// Wait and try again
					try {
						System.out.println("Timeout on TCP. Will Re-try to connect in 1s");
						closeReader();
						Thread.sleep(1000L);
						System.out.println("Re-trying now. (from " + this.getClass().getName() + ")");
						read();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else
					manageError(se);
			}
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
	}

	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			if (skt != null) {
				this.goRead = false;
				skt.close();
				skt = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for TCP */ }

	public static void main(String[] args) {
		System.setProperty("verbose", "true");
		String host = "localhost";
		int port = 2947;
		try {
			boolean keepTrying = true;
			while (keepTrying) {
				TCPClient tcpClient = new TCPClient(host, port);
				System.out.println(new Date().toString() + ": New " + tcpClient.getClass().getName() + " created.");

				try {
					tcpClient.read("Yo!");

					tcpClient.read("?WATCH={...};");

					tcpClient.read("exit");

				} catch (Exception ex) {
					System.err.println("TCP Reader:" + ex.getMessage());
					ex.printStackTrace();

					tcpClient.closeReader();
					long howMuch = 1000L;
					System.out.println("Will try to reconnect in " + Long.toString(howMuch) + "ms.");
					try {
						Thread.sleep(howMuch);
					} catch (InterruptedException ie) {
					}
				}
				keepTrying = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
