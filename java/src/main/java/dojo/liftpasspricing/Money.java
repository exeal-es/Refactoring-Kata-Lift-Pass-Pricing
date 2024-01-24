package dojo.liftpasspricing;
public class Money
{	private final double cost;
	public Money( double cost)
	{
		this.cost = cost;
	}

	Money roundUp() {
	  return new Money(Math.ceil(getCost()));
	}

	public double   getCost() {
  return cost;
}

	public int asInt(){
		return (int) cost;
	}
}


