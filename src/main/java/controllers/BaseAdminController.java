package controllers;

public abstract class BaseAdminController {
    protected MainController mainController;
    
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    
    public MainController getMainController() {
        return mainController;
    }
} 