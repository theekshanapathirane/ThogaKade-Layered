package controller;

import bo.BoFactory;
import bo.custom.CustomerBo;
import bo.custom.OrderBo;
import bo.custom.impl.CustomerBoImpl;
import bo.custom.impl.OrderBoImpl;
import bo.custom.ItemBo;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.util.BoType;
import dto.CustomerDto;
import dto.ItemDto;
import dto.OrderDetailsDto;
import dto.OrderDto;
import dto.tm.OrderTm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import dao.custom.ItemDao;
import dao.custom.impl.ItemDaoImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PlaceOrderFormController {

    public AnchorPane pane;
    public Label max;
    @FXML
    private JFXComboBox<?> cmbCustId;

    @FXML
    private JFXComboBox<?> cmbItemCode;

    @FXML
    private JFXTextField txtCustName;

    @FXML
    private JFXTextField txtDesc;

    @FXML
    private JFXTextField txtUnitPrice;

    @FXML
    private JFXTextField txtQty;

    @FXML
    private JFXTreeTableView<OrderTm> tblOrder;

    @FXML
    private TreeTableColumn<?, ?> colCode;

    @FXML
    private TreeTableColumn<?, ?> colDesc;

    @FXML
    private TreeTableColumn<?, ?> colQty;

    @FXML
    private TreeTableColumn<?, ?> colAmount;

    @FXML
    private TreeTableColumn<?, ?> colOption;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblOrderId;


    private List<CustomerDto> customers;
    private List<ItemDto> items;
    private List<ItemDto>cart=new ArrayList<>();

    private CustomerBo customerBo =new CustomerBoImpl();
    private OrderBo orderBo=new OrderBoImpl();
    private ItemDao itemDao =new ItemDaoImpl();
    private ObservableList<OrderTm>tmList=FXCollections.observableArrayList();

    double tot=0;
    private ItemBo itemBo = BoFactory.getInstance().getBo(BoType.ITEM);
    @FXML
    void addToCartButtonOnAction(ActionEvent event) {
        String id = cmbItemCode.getSelectionModel().getSelectedItem().toString();
        int txt =0;
        if(!(txtQty.getText().isEmpty())){
            txt =Integer.parseInt(txtQty.getText());
        }
        for (ItemDto set : items) {
            if (set.getCode().equals(id)) {
                if (!(set.getQty() >= txt||1<=txt)) {
                    new Alert(Alert.AlertType.ERROR, "Check QTY!").show();
                } else {
                    txtQty.setStyle("-fx-border-color:white");
                    max.setVisible(false);

        try {
            double amount= itemDao.getItem(cmbItemCode.getValue().toString()).getUnitPrice() * Integer.parseInt(txtQty.getText());
            JFXButton btn =new JFXButton("Delete");
            OrderTm tm =new OrderTm(
                    cmbItemCode.getValue().toString(),
                    txtDesc.getText(),
                    Integer.parseInt(txtQty.getText()),
                    amount,
                    btn);
            boolean isExist=false;

            btn.setOnAction(actionEvent -> {
                tmList.remove(tm);
                tblOrder.refresh();
                tot-=tm.getAmount();
                lblTotal.setText(String.format("%.2f",tot));
                replace(tm.getCode());

            });

            for (OrderTm order:tmList){
                if(order.getCode().equals(tm.getCode())) {
                    order.setQty(order.getQty() + tm.getQty());
                    order.setAmount(order.getAmount() + tm.getAmount());
                    isExist = true;
                    tot += tm.getAmount();
                }

            }
            if(!isExist){
                tmList.add(tm);
                tot+=tm.getAmount();

            }
            TreeItem<OrderTm> treeItem = new RecursiveTreeItem<OrderTm>(tmList, RecursiveTreeObject::getChildren);
            tblOrder.setRoot(treeItem);
            tblOrder.setShowRoot(false);

            lblTotal.setText(String.format("%.2f",tot));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
try {
    cart.add(new ItemDto(
            cmbItemCode.getValue().toString(),
            txtDesc.getText(),
            Double.parseDouble(txtUnitPrice.getText()),
            Integer.parseInt(txtQty.getText())
    ));
    for (ItemDto dto : items) {
        if (dto.getCode().equals(cmbItemCode.getValue().toString())) {
            dto.setQty(dto.getQty() - Integer.parseInt(txtQty.getText()));
        }
    }

}catch (NullPointerException e){
    e.printStackTrace();
    System.out.println("hello");
}
        txtQty.clear();
                }
            }
        }


    }

    private void replace(String code) {
        for (ItemDto dto : items) {
            if (dto.getCode().equals(code)){
                for (ItemDto pack : cart) {
                    if (pack.getCode().equals(code)) {
                        dto.setQty(dto.getQty() + pack.getQty());
                    }
                }
            }
        }
    }

    @FXML
    void backButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/DashboardForm.fxml"))));
            stage.show();
            cart.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void generateId(){

        lblOrderId.setText(orderBo.generateId());
    }

    @FXML
    void placeOrderButtonOnAction(ActionEvent event) {
        if(!(txtCustName.getText().isEmpty())) {
            List<OrderDetailsDto> list = new ArrayList<>();

            for (OrderTm tm : tmList) {
                list.add(new OrderDetailsDto(
                        lblOrderId.getText(),
                        tm.getCode(),
                        tm.getQty(),
                        tm.getAmount() / tm.getQty()
                ));
            }
            if (!tmList.isEmpty()) {
                boolean isSaved = false;
                try {
                    isSaved = orderBo.saveOreder(new OrderDto(
                            lblOrderId.getText(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")),
                            cmbCustId.getValue().toString(),
                            list
                    ));

                    if (isSaved) {
                        for (ItemDto cart : cart) {
                                for (ItemDto dto :items) {
                                    if (cart.getCode().equals(dto.getCode())) {
                                        itemDao.removeItem(dto.getQty(),cart.getCode() );
                                    }
                                }

                        }
                        cart.clear();
                        refresh();
                        new Alert(Alert.AlertType.INFORMATION, "Order Saved !").show();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Something went Wrong !").show();
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }else{
            new Alert(Alert.AlertType.ERROR, "Customer not Found !").show();
        }
    }

    private void refresh() {
        tmList.clear();
        tblOrder.refresh();
       loadCustomerId();
        loadItemCodes();
        txtQty.clear();
        cmbload();
        generateId();


    }

    public void initialize(){
        colCode.setCellValueFactory(new TreeItemPropertyValueFactory<>("code"));
        colDesc.setCellValueFactory(new TreeItemPropertyValueFactory<>("desc"));
        colQty.setCellValueFactory(new TreeItemPropertyValueFactory<>("qty"));
        colAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("amount"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));


        generateId();
        loadCustomerId();
        loadItemCodes();
        txtUnitPrice.setEditable(false);
        txtDesc.setEditable(false);
        txtCustName.setEditable(false);


        cmbload();



    }

    private void cmbload() {
        cmbCustId.getSelectionModel().selectedItemProperty().addListener(((observableValue,oldValue,id) -> {
            for (CustomerDto dto : customers) {
                if (dto.getId().equals(id)){
                    txtCustName.setText(dto.getAddress());
                }
            }
        }));
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(((observableValue,oldValue,id) -> {
            for (ItemDto dto : items) {
                if (dto.getCode().equals(id)){
                    txtDesc.setText(dto.getDesc());
                    txtUnitPrice.setText(String.valueOf(dto.getUnitPrice()));
                }
            }
        }));
    }

    private void loadItemCodes(){
        try {
            items= itemBo.allItems();
            ObservableList list= FXCollections.observableArrayList();
            for(ItemDto dto:items){
                list.add(dto.getCode());
            }
            cmbItemCode.setItems(list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadCustomerId() {
        try {
            customers= customerBo.allCustomers();
            ObservableList list= FXCollections.observableArrayList();
            for(CustomerDto dto:customers){
                list.add(dto.getId());
            }
            cmbCustId.setItems(list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void checkAvl(KeyEvent keyEvent) {

        String id = cmbItemCode.getSelectionModel().getSelectedItem().toString();
            int txt =0;
             if(!(txtQty.getText().isEmpty())){
                 txt =Integer.parseInt(txtQty.getText());
             }
            for (ItemDto dto : items) {
                if (dto.getCode().equals(id)) {
                    if (!(dto.getQty() > txt)) {
                        max.setVisible(true);
                        max.setText("avl " + dto.getQty());
                        txtQty.setStyle("-fx-border-color:red");

                    } else {
                        txtQty.setStyle("-fx-border-color:white");
                        max.setVisible(false);
                    }
                }
            }

    }
}
