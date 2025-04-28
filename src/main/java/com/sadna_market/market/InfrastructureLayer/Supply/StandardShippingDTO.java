public class StandardShippingDTO implements SupplyMethod {
    public String carrier;
    public int estimatedDays;

    public StandardShippingDTO(String carrier, int estimatedDays) {
        this.carrier = carrier;
        this.estimatedDays = estimatedDays;
    }

    @Override
    public boolean accept(SupplyVisitor visitor, String address, double weight) {
        return visitor.visit(this, address, weight);
    }
}