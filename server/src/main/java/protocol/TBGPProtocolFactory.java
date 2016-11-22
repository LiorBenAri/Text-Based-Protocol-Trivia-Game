package protocol;

public class TBGPProtocolFactory implements ServerProtocolFactory<String> {


	public AsyncServerProtocol<String> create() {
		TBGPProtocol p = new TBGPProtocol();
		return p;
	}

}
