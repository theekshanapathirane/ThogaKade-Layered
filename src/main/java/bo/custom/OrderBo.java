package bo.custom;

import bo.SuperBo;
import dto.OrderDto;

import java.sql.SQLException;

public interface OrderBo extends SuperBo {
   boolean saveOreder(OrderDto dto)throws SQLException,ClassNotFoundException;
   String generateId() ;

}
