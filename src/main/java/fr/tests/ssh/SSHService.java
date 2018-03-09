package fr.tests.ssh;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The SSH service to use when you need to interrogate a remote server with SSH protocol
 */
public interface SSHService extends Closeable {
	
	/**
	 * The default SSH port is 22
	 */
	public static final int DEFAULT_SSH_PORT = 22;
	
	/**
	 * The default connection timeout is 5 seconds 
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

	/**
	 * Connection method based on username and password
	 * @param username the username
	 * @param host the host (IP or DNS name of the remote server to reach)
	 * @param password the password
	 * @throws IOException 
	 * @see {@link SSHService#DEFAULT_SSH_PORT}
	 * @see {@link SSHService#DEFAULT_CONNECTION_TIMEOUT}
	 */
	public default void connect(String username, String host, String password) throws IOException {
		this.connect(username, host, password, null, null, DEFAULT_SSH_PORT, DEFAULT_CONNECTION_TIMEOUT);
	}
	
	/**
	 * Connection method base on username, private key and passphrase (not mandatory)
	 * @param username the username
	 * @param host the host (IP or DNS name of the remote server to reach)
	 * @param privateKeyFilePath the path the private key (null if you don't need a private key)
	 * @param passphrase the passphrase used with the private key if necessary (null otherwise)
	 * @throws IOException
	 */
	public default void connect(String username, String host, String privateKeyFilePath, String passphrase) throws IOException {
		this.connect(username, host, null, privateKeyFilePath, passphrase, DEFAULT_SSH_PORT, DEFAULT_CONNECTION_TIMEOUT);
	}
	
	/**
	 * Full SSH connection method
	 * @param username the username
	 * @param host the host (IP or DNS name of the remote server to reach)
	 * @param password the password (null if using a private key)
	 * @param privateKeyFilePath the path the private key (null if you don't need a private key)
	 * @param passphrase the passphrase used with the private key if necessary (null otherwise)
	 * @param port the port to use (default = 22)
	 * @param timeout the connection timeout in ms (default = 5000ms)
	 * @throws IOException 
	 */
	public void connect(String username, String host, String password, String privateKeyFilePath, String passphrase, int port, int timeout) throws IOException;

	/**
	 * Execute an SSH command an wrapp the result into a SSHCommandResult
	 * @param command the command to execute on the remote server
	 * @return and SSHCommandResult
	 * @throws IOException if any issue occurs while executing the SSH command 
	 * @see {@link SSHCommandResult}
	 */
	public SSHCommandResult executeCommand(String command) throws IOException;
	
	/**
	 * Return a Future concurrent object to perform the execution in a separate thread. Example :
	 * <p>
	 * <code>
	 * try {<br />
	 * &nbsp;&nbsp;&nbsp;Future&lt;SSHCommandResult&gt; future = sshService.getFutureForCommand("ls -l");<br />
	 * &nbsp;&nbsp;&nbsp;SSHCommandResult result = future.get();<br />
	 * } catch (InterruptedException | ExecutionException e) {<br />
	 * &nbsp;&nbsp;&nbsp;// Manage exceptions<br />
	 * }
	 * </code>
	 * </p>
	 * @param command the command to execute on the remote server
	 * @return an instance of Future with SSHCommandResult as a result type
	 */
	public default Future<SSHCommandResult> getFutureForCommand(String command) {
		
		Callable<SSHCommandResult> task = () -> executeCommand(command);
		ExecutorService executor = Executors.newSingleThreadExecutor();
	
		return executor.submit(task);
	}
	
	/**
	 * Same as {@link SSHService#getFutureForCommand(String)} but with a list of commands to execute. The value returned
	 * by the future object is a list of SSHCommandResult
	 * @param commands an array of commands
	 * @return an instance of Future with List&lt;SSHCommandResult&gt; as a result type.
	 */
	public default Future<List<SSHCommandResult>> getFutureForCommands(String... commands) {
		
		return getFutureForCommands(Arrays.asList(commands));
	}
	
	/**
	 * Same as {@link SSHService#getFutureForCommand(String)} but with a list of commands to execute. The value returned
	 * by the future object is a list of SSHCommandResult
	 * @param commands a list of commands
	 * @return an instance of Future with List&lt;SSHCommandResult&gt; as a result type.
	 */
	public default Future<List<SSHCommandResult>> getFutureForCommands(List<String> commands) {
		
		Callable<List<SSHCommandResult>> task = () -> {
			
			List<SSHCommandResult> results = new ArrayList<SSHCommandResult>(commands.size()); 
			
			for(String command : commands) {
				results.add(executeCommand(command));
			}
			return results;
		};
		ExecutorService executor = Executors.newSingleThreadExecutor();

		return executor.submit(task);
	}
}
