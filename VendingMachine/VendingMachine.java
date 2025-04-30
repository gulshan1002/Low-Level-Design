import java.util.*;
import java.io.*;
import java.util.stream.*;
class Product{
	private int id;
	private String name;
	private int price;
	public Product(int id, String name, int price){
		this.id = id;
		this.name = name;
		this.price = price;
	}
	public int getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public int getPrice() {
		return this.price;
	}
}
class PaymentService{
	private int amount;
	
	public PaymentService() {
		this.amount = 0;
	}
	public void insertMoney(int money, Map<Product, Integer>productCart) {
		this.amount+=money;
		int cartValue = calculateCurrentCartPrice(productCart);
		if(amount<cartValue) {
			System.out.println("Insufficient balance!");
			return;
		}
		 else {
			 StringBuilder items = new StringBuilder();
	         for (Map.Entry<Product, Integer> entry : productCart.entrySet()) {
	        	 items.append(entry.getKey().getName()).append(" ");
	           }
	           System.out.println(String.format("Payment received: %d. Dispensing %sReturning change: %d", amount, items.toString(), amount - cartValue));
	           resetAmount();
		 }
	}
	public void resetAmount() {
		this.amount = 0;
	}
	public int calculateCurrentCartPrice(Map<Product, Integer>productCart) {
		int cartValue = 0;
		for(Map.Entry<Product, Integer>entry:productCart.entrySet()) {
			Product currProduct = entry.getKey();
			int price = currProduct.getPrice();
			int factor = entry.getValue();
			cartValue+=price*factor;
		}
		return cartValue;
	}
	
}
class InventoryService{
	Map<Integer, Integer>products = new HashMap<>();
	public void registerStock(int itemCode, int quantity) {
		products.put(itemCode, quantity);
	}
	public void addStock(int itemCode, int quantity) {
		int currVal = products.getOrDefault(itemCode, 0);
		currVal+=quantity;
		products.put(itemCode, currVal);
		
	}
	public int getCurrentQuantity(int itemCode) {
		return products.getOrDefault(itemCode, 0);
	}
	public Map<Integer, Integer> getProductQuantityMap(){
		return this.products;
	}
}
class Shop{
	InventoryService inventoryService;
	PaymentService paymentService;
	Map<Integer, Product>productMap;
	Map<Product, Integer>productCart;
	
	public Shop() {
		inventoryService = new InventoryService();
		paymentService = new PaymentService();
		productMap = new HashMap<>();
		productCart = new HashMap<>();
	}
	public void viewItems() {
		Map<Integer, Integer> productsQuantityMap = inventoryService.getProductQuantityMap();
		List<Product> products = new ArrayList<Product>();
		for(Map.Entry<Integer, Product>entry:productMap.entrySet()) {
			products.add(entry.getValue());
		}
		if(products.size()==0) {
			System.out.println("No items available");
            return;
		}
		else {
			for (Product product : products) {
	            int quantity = productsQuantityMap.getOrDefault(product.getId(), 0);
	            System.out.println(String.format("%d %s %d %d", product.getId(), product.getName(), product.getPrice(), quantity));
	        }
			
		}
	}
	public void registerItems(String name, int price, int quantity) {
		Product newProduct = new Product(productMap.size()+1, name, price);
		productMap.put(newProduct.getId(), newProduct);
		inventoryService.registerStock(newProduct.getId(), quantity);
		System.out.println(String.format("Item added successfully, item_code: %d", newProduct.getId()));
	}
	public void addStocks(int itemCode, int quantity) {
		inventoryService.addStock(itemCode, quantity);
	}
	public void selectItems(int itemCode) {
		Product product = productMap.get(itemCode);
		Map<Integer, Integer>productQuantityMap = inventoryService.getProductQuantityMap();
		int currStock = inventoryService.getCurrentQuantity(itemCode);
		if(currStock==0) {
			System.out.println(String.format("Out Of Stock!, Item_code %d", itemCode));
			return;
		}
		productCart.put(product, productCart.getOrDefault(product, 0) + 1);
		productQuantityMap.put(itemCode, currStock - 1);
	     System.out.println(String.format("You selected: %s %d", product.getName(), product.getPrice()));
	}
	public void addMoney(int amount) {
		if(productCart.isEmpty()) {
			System.out.println("Please select a item first");
            return;
		}
		paymentService.insertMoney(amount, productCart);
	}
	public void cancel() {
		Map<Integer, Integer>productQuantityMap = inventoryService.getProductQuantityMap();
		for(Map.Entry<Product, Integer>entry:productCart.entrySet()) {
			int itemCode = entry.getKey().getId();
			int cartValue = entry.getValue();
			int currValue = productQuantityMap.getOrDefault(itemCode, 0);
			productQuantityMap.put(itemCode, cartValue+currValue);
		}
		productCart = new HashMap<>();
		paymentService.resetAmount();
		System.out.println("Transaction Cancelled!");
		return;
	}
	
}
class UI{
	public static void interact(List<String>aargs) {
		Shop shop = new Shop();
		for(String args: aargs) {
			String[] part = args.split(" ");
			String command = part[0];
			switch(command) {
				case "VIEW_ITEMS":
					shop.viewItems();
					break;
				case "REGISTER_ITEMS":
					shop.registerItems(part[1], Integer.parseInt(part[2]), Integer.parseInt(part[3]));
					break;
				case "ADD_STOCKS":
					shop.addStocks(Integer.parseInt(part[1]), Integer.parseInt(part[2]));
					break;
				case "SELECT_ITEMS":
					shop.selectItems(Integer.parseInt(part[1]));
					break;
				case "ADD_MONEY":
					shop.addMoney(Integer.parseInt(part[1]));
					break;
				case "CANCEL":
					shop.cancel();
					break;
			}
		}
	}
}
public class VendingMachine {

	public static void main(String[] args) throws IOException{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		int argsCount = Integer.parseInt(bufferedReader.readLine().trim());

        List<String> aargs = IntStream.range(0, argsCount).mapToObj(i -> {
            try {
                return bufferedReader.readLine();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
        UI.interact(aargs);
	}
}
