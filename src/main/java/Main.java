

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fr.asenka.ssh.service.SSHCommandResult;
import fr.asenka.ssh.service.SSHService;
import fr.asenka.ssh.service.impl.SSHServiceImpl;

public class Main {

	
	public static void main(String args[]) {
		
		SSHService sshService = new SSHServiceImpl();

		try {
			System.out.print("Connection...");
//			sshService.connect("romain", "192.168.56.101", "C:/key-centos_openssh.ppk", "coucou");
			sshService.connect("romain", "192.168.56.101", "romain");
			
			System.out.println("OK.");
			

			ExecutorService executor = Executors.newSingleThreadExecutor();
			
			Future<SSHCommandResult> future = executor.submit(() -> sshService.executeCommand("ls -l"));
			
			System.out.println(formatResults(future.get()));
			
			executor.shutdown();
			
		} catch (IOException | InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Close SSH connection...");
			try {
				sshService.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Exit test program.");
		}
	}
	
	private static final String formatResults(SSHCommandResult... results) {
		
		StringBuffer resultBuffer = new StringBuffer();
		
		for(SSHCommandResult result : results) {
			
			resultBuffer.append("Command:\n");
			resultBuffer.append("> ");
			resultBuffer.append(result.getCommand());
			resultBuffer.append("\n");
			resultBuffer.append(result.getResponse());
			resultBuffer.append("[exit status: " + result.getExitStatus() + "]\n");
			resultBuffer.append("\n");
		}
		return resultBuffer.toString();
	}
}
