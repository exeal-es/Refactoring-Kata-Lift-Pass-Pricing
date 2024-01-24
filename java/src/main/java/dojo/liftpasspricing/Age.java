package dojo.liftpasspricing;
public class Age
{	private final Integer age;
	public Age( Integer age)
	{
		this.age = age;
	}

	boolean isSenior() {
		return age > 64;
	}

}
