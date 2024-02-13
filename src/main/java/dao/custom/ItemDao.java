package dao.custom;


import dao.CrudDao;
import dto.ItemDto;
import entity.Item;

import java.sql.SQLException;
import java.util.List;

public interface ItemDao extends CrudDao<Item> {
//    boolean saveItem(Item entity) throws SQLException, ClassNotFoundException;
//
//    boolean updateItem(Item entity) throws SQLException, ClassNotFoundException;
//
//    boolean deleteItem(String id) throws SQLException, ClassNotFoundException;
//    List<Item> allItem() throws SQLException, ClassNotFoundException;

    ItemDto getItem(String code) throws SQLException, ClassNotFoundException;

    void removeItem(int num ,String code) throws SQLException, ClassNotFoundException;

}
