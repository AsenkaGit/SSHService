package fr.tests.ssh;


/**
 * A result of a remote command made with SSH
 */
public class SSHCommandResult {

	/**
	 * The original command
	 */
	private String command;

	/**
	 * The response of the command
	 */
	private String response;

	/**
	 * The exit status of the command (usually 0 if everything is okay)
	 */
	private Integer exitStatus;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Integer getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(Integer exitStatus) {
		this.exitStatus = exitStatus;
	}

	@Override
	public String toString() {
		return "Result [command=" + command + ", response=" + response + ", exitStatus=" + exitStatus + "]";
	}

}