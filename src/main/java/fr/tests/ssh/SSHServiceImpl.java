package fr.tests.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Implementation of the SSHService based on the <b>JSch</b> library.
 * <ul>
 * <li>Website:
 * <a href="http://www.jcraft.com/jsch/">http://www.jcraft.com/jsch/</a></li>
 * <li>Javadoc: <a href=
 * "https://epaul.github.io/jsch-documentation/javadoc/">https://epaul.github.io/jsch-documentation/javadoc/</a></li>
 * </ul>
 * 
 * @see SSHService
 */
public class SSHServiceImpl implements SSHService {

	/**
	 * Value used to create the execution channel
	 * 
	 * @see Channel
	 * @see ChannelExec
	 * @see Session#openChannel(String)
	 */
	private static final String EXEC_CHANNEL_TYPE = "exec";

	/**
	 * The session used to connect with SSH to the remote server
	 */
	private Session session;

	@Override
	public void connect(String username, String host, String password, String privateKeyFilePath, String passphrase, int port, int timeout) throws IOException {

		try {
			// Prepare and connect the session
			final JSch jsch = new JSch();
			
			// If we use a private key authentification method
			if(privateKeyFilePath != null) {
				jsch.addIdentity(privateKeyFilePath, passphrase == null ? "" : passphrase);
			}
			this.session = jsch.getSession(username, host, port);
			
			// If we are NOT using private key authentification method, then we need the password
			if(password != null && privateKeyFilePath == null) {
				this.session.setPassword(password);
			}
			this.session.setUserInfo(new DefaultUserInfo());
			this.session.connect(timeout);

		} catch (JSchException e) {
			throw new IOException(e);
		}
	}

	@Override
	public SSHCommandResult executeCommand(String command) throws IOException {

		if (this.session != null) {

			try {
				// Prepare and connection the channel with the requested command
				ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
				channel.setCommand(command);
				channel.setInputStream(null);
				channel.connect();

				// Read the command response and create the Result
				SSHCommandResult commandResult = getResult(channel);
				commandResult.setCommand(command);

				// Disconnect the execution channel and return the result
				channel.disconnect();
				return commandResult;

			} catch (JSchException e) {
				throw new IOException(e);
			}
		} else {
			throw new IOException("SSH session not ready to execute commands");
		}

	}

	/**
	 * Disconnect the SSH session. Do not forget to use this method when you are
	 * done with the SSH server
	 */
	@Override
	public void close() throws IOException {

		if (this.session != null) {

			this.session.disconnect();
		}
	}

	/**
	 * Get the response from a connected execution channel
	 * 
	 * @param channel the channel ready to perform its command
	 * @return an instance of SSHCommandResult
	 * @throws IOException if the channel is not connected or if there is any issue
	 *             reading the response
	 */
	private SSHCommandResult getResult(final ChannelExec channel) throws IOException {

		if (channel.isConnected()) {

			final SSHCommandResult commandResult = new SSHCommandResult();
			final InputStream in = channel.getInputStream();
			final StringBuffer responseBuffer = new StringBuffer();
			final byte[] buffer = new byte[1024];

			// While there is something to read on the stream
			while (true) {

				while (in.available() > 0) {

					int i = in.read(buffer, 0, 1024);

					if (i < 0) {
						break;
					}
					responseBuffer.append(new String(buffer, 0, i));
				}

				if (channel.isClosed()) {

					if (in.available() > 0) {
						continue;
					}
					commandResult.setExitStatus(channel.getExitStatus());
					break;
				}

				// Wait a little bit...
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
					throw new IOException(ex);
				}
			}
			commandResult.setResponse(responseBuffer.toString());
			return commandResult;
		} else {
			throw new IOException("The channel is not connected.");
		}
	}

	/**
	 * Implementation of UserInfo necessary to manage RSA fingerprint confirmation.
	 */
	private class DefaultUserInfo implements UserInfo {

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return true;
		}

		@Override
		public boolean promptPassword(String message) {
			return false;
		}

		/**
		 * This method always return <code>true</code> to confirm the SSH connection
		 * 
		 * @param message
		 * @return <code>true</code>
		 */
		@Override
		public boolean promptYesNo(String message) {
			return true;
		}

		@Override
		public void showMessage(String message) {

		}
	}
}
