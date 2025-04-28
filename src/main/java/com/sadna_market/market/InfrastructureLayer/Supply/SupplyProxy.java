public class SupplyProxy implements SupplyInterface {
    private SupplyAdapter adapter = new SupplyAdapter();

    @Override
    public boolean shipStandard(StandardShippingDTO standardShipping, String address, double weight) {
        System.out.println("Proxy: Logging standard shipping request...");
        return adapter.shipStandard(standardShipping, address, weight);
    }

    @Override
    public boolean shipExpress(ExpressShippingDTO expressShipping, String address, double weight) {
        System.out.println("Proxy: Logging express shipping request...");
        return adapter.shipExpress(expressShipping, address, weight);
    }

    @Override
    public boolean arrangePickup(PickupDTO pickup, String address, double weight) {
        System.out.println("Proxy: Logging pickup request...");
        return adapter.arrangePickup(pickup, address, weight);
    }
}