public class ExternalSupplyAPI {
    public boolean sendStandardShippingRequest(String carrier, String address, double weight, int estimatedDays) {
        System.out.println("External API: Arranging standard shipping to " + address + " with " + carrier);
        System.out.println("Estimated delivery: " + estimatedDays + " days");
        return true;
    }

    public boolean sendExpressShippingRequest(String carrier, String address, double weight, int priorityLevel) {
        System.out.println("External API: Arranging express shipping to " + address + " with " + carrier);
        System.out.println("Priority level: " + priorityLevel);
        return true;
    }

    public boolean registerPickupRequest(String location, String pickupCode, double weight) {
        System.out.println("External API: Registering pickup at " + location);
        System.out.println("Pickup code: " + pickupCode);
        return true;
    }
}