package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.MessageService;
import com.sadna_market.market.DomainLayer.DomainServices.OrderProcessingService;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;

public class MarketSystemConfig {

    public static MarketSystemConfig instance;

    //Repositories
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private IOrderRepository orderRepository;
    private IReportRepository reportRepository;
    private IStoreRepository storeRepository;
    private IMessageRepository messageRepository;

    //Domain Services
    private MessageService messageService;
    private OrderProcessingService orderProcessingService;
    private StoreManagementService storeManagementService;
    private UserAccessService userAccessService;

    //Application Services
    private ProductService productService;
    private StoreService storeService;
    private UserService userService;
    private MessageApplicationService messageApplicationService;

    //Market System
    private MarketService marketService;

    private MarketSystemConfig(){

        initializeRepositories();
        initializeDomainServices();
        initializeApplicationServices();
        initializeMarketService();

    }

    public static synchronized MarketSystemConfig getInstance() {
        if(instance == null) {
            instance = new MarketSystemConfig();
        }
        return instance;
    }

    private void initializeRepositories {



    }




}
