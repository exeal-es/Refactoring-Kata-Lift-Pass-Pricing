package dojo.liftpasspricing;
public class Age
{	private final Integer age;
	public Age( Integer age)
	{
		this.age = age;
	}

	boolean isUnknown() {
		return age == null;
	}

	boolean isTeenager() {
		if (age == null)
			return false;
		return age < 15;
	}

	boolean isChild() {
		if (age == null)
			return false;
		return age < 6;
	}

	boolean isSenior() {
		return age > 64;
	}

}
