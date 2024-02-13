package dao.custom;

import dto.OrderDetailsDto;
import entity.Orders;
import org.hibernate.criterion.Order;

import java.sql.SQLException;
import java.util.List;

public interface OrderDetailsDao {
    boolean saveOrderDetails(List<OrderDetailsDto> list) throws SQLException, ClassNotFoundException;
}
