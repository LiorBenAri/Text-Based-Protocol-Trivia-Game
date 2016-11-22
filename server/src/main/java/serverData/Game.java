package serverData;

import java.io.IOException;

public interface Game {
	public void TxtResp(String nick, String resp) throws IOException;
	public void SelectResp(String nick, int resp) throws IOException;
}
