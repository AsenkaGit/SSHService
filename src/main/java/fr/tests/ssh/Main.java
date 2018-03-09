package fr.tests.ssh;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

	
	public static void main(String args[]) {
		
		SSHService sshService = new SSHServiceImpl();

		try {
			System.out.print("Connection...");
			sshService.connect("romain", "192.168.56.101", "C:/key-centos_openssh.ppk", "coucou");
//			sshService.connect("romain", "192.168.56.101", "romain");
			System.out.println("OK.");
			
			Future<List<SSHCommandResult>> future = sshService.getFutureForCommands("ls -l", "uname -a");
			
			System.out.println(future.get());
			
			
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
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
}
