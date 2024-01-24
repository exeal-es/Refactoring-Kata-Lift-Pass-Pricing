package dojo.liftpasspricing;
public class StayType
{	private final String stayType;
	public StayType( String stayType)
	{
		this.stayType = stayType;
	}

	boolean isNight() {
		return stayType
		  .equals("night");
	}

}
