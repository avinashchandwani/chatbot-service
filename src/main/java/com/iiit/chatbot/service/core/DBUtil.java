package com.iiit.chatbot.service.core;

import com.iiit.chatbot.service.entity.Item;
import com.iiit.chatbot.service.entity.Order;

import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DBUtil {

    private Connection con;


    public static void main(String[] args) {

        DBUtil u = new DBUtil();
        u.openConnection();
        u.closeConnection();
    }

    private void openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/nlp?characterEncoding=UTF-8&useSSL=false", "root", "nirankar@123");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private void commit() {
        try {
            con.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (con != null || !con.isClosed())
                con.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrderStatus(int userId) {
        List<Order> orders = new LinkedList<Order>();
        Statement stmt = null;
        openConnection();
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT o.* from orders o where o.user_id = " + userId + " and order_status<>'CANCELLED'");
            while (rs.next()) {
                Order order = new Order();
                order.setOrderNumber(rs.getString(1));
                order.setItemName("" + rs.getInt(2));
                order.setQty(rs.getInt(3));
                order.setOrderDate(rs.getDate(4));
                order.setStatus(rs.getString(6));
                order.setSuccess(true);
                orders.add(order);
            }
        } catch (Exception ex) {
            System.err.println("Error occured while fetching order details");
            ex.printStackTrace();
        }
        return orders;
    }

    public Order getOrderStatus(String orderNumberStr) {
        Order order = new Order();
        int orderNumber = Integer.parseInt(orderNumberStr.split("#")[1]);
        Statement stmt = null;
        try {
            openConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ORDERS where order_number=" + orderNumber);
            while (rs.next()) {
                order.setOrderNumber(rs.getString(1));
                order.setItemName("" + rs.getInt(2));
                order.setQty(rs.getInt(3));
                order.setOrderDate(rs.getDate(4));
                order.setStatus(rs.getString(6));
                order.setSuccess(true);
            }
        } catch (Exception ex) {
            return null;
        }
        closeConnection();
        return order;
    }

    public Order placeOrder(String itemName, int qty) {
        Order order = new Order();
        Item item = null;
        int itemAvailabilityId = 0;
        int itemAvailabilityQty = 0;
        boolean isAvailable = false;
        Statement stmt = null;
        try {
            openConnection();
            con.setAutoCommit(false);
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ITEM WHERE ITEM_NAME='" + itemName + "'");
            while (rs.next()) {
                item = new Item();
                item.setItemName(rs.getString(1));
                item.setItemId(rs.getInt(2));
            }
            if (item == null) {
                return null;
            }
            rs = stmt.executeQuery("SELECT * FROM ITEM_AVAILABILITY WHERE ITEM_ID = " + item.getItemId() + " order by item_lead_time and item_quantity>0");
            int leadTime = 0;
            while (rs.next()) {
                int availableQty = rs.getInt(4);
                if (availableQty <= 0) {
                    continue;
                } else {
                    itemAvailabilityId = rs.getInt(1);
                    itemAvailabilityQty = rs.getInt(4) - 1;
                    isAvailable = true;
                    leadTime = rs.getInt(5);
                    order.setItemName(item.getItemName());
                    break;
                }
            }
            int nextId = 0;
            if (isAvailable) {
                stmt.execute("UPDATE ITEM_AVAILABILITY SET item_quantity = " + itemAvailabilityQty + " WHERE item_availability_id =  " + itemAvailabilityId);
                // Get Max of order Number
                // put it as ID and
                rs = stmt.executeQuery("SELECT COALESCE(max(order_number),0) from ORDERS");

                while (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                }
                Date today = new Date();
                Date dayAfter = new Date(today.getTime() + TimeUnit.DAYS.toMillis(leadTime));
                java.sql.Date sqlDate = new java.sql.Date(dayAfter.getTime());
                stmt.execute("INSERT INTO ORDERS VALUES(" + nextId + "," + item.getItemId() + ",1,'" + sqlDate + "',1,'DISPATCHED')");
            }
            storeOrderConsumption(nextId, itemAvailabilityId);
            commit();
            order = getOrderStatus("ORDR#" + nextId);
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeConnection();
        return order;
    }

    public Order cancelOrder(String orderNumberStr) {
        Order order = new Order();
        Statement stmt = null;
        int orderNumber = Integer.parseInt(orderNumberStr);
        openConnection();
        try {
            stmt = con.createStatement();
            int rows = stmt.executeUpdate("UPDATE ORDERS SET order_status='CANCELLED' WHERE order_number=" + orderNumber);
            if (rows > 0) {
                order.setSuccess(true);
                updateCancelledConsumptionAvailability(orderNumber);
                cancelOrderConsumption(orderNumber);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeConnection();
        return order;
    }

    public int getUserId(String userName) {
        openConnection();
        Statement stmt = null;
        int userId = 0;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id from user where user_name ='" + userName.trim() + "'");
            while (rs.next()) {
                userId = rs.getInt(1);
            }
        } catch (Exception ex) {
            userId = -1;
        }
        closeConnection();
        return userId;
    }

    private int getCurrentOrderConsumptionId() {
        Statement stmt = null;
        int userId = 0;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COALESCE(max(order_cons_id),1) from order_consumption");
            while (rs.next()) {
                userId = rs.getInt(1);
            }
        } catch (Exception ex) {
            userId = -1;
            ex.printStackTrace();
        }
        return userId;
    }

    private boolean storeOrderConsumption(int orderId, int itemAvailabilityId) {
        boolean status = false;
        PreparedStatement stmt = null;
        try {
            int orderConsumptionId = getCurrentOrderConsumptionId() + 1;
            stmt = con.prepareStatement("INSERT INTO order_consumption VALUES (?,?,?)");
            stmt.setInt(1, orderConsumptionId);
            stmt.setInt(2, orderId);
            stmt.setInt(3, itemAvailabilityId);
            status = stmt.execute();
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        return status;
    }

    private boolean cancelOrderConsumption(int orderId) {

        boolean status = false;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM order_consumption where order_id=" + orderId);
            status = stmt.execute();
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        return status;
    }

    private boolean updateCancelledConsumptionAvailability(int orderNumber) {

        boolean status = false;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        int itemAvailabilityId = 0;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT item_availibility_id from order_consumption where order_id =" + orderNumber);
            while (rs.next()) {
                itemAvailabilityId = rs.getInt(1);
            }
            pstmt = con.prepareStatement("UPDATE item_availability set item_quantity = item_quantity+1 where item_availability_id = " + itemAvailabilityId);
            status = pstmt.execute();
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        return status;
    }

    public boolean logChatWithUser(int userId, String userQuery, String botAnswer, byte helpful) {

        openConnection();
        PreparedStatement stmt = null;
        boolean success = false;
        Date date = new Date();
        java.sql.Timestamp sqlTimeStamp = new java.sql.Timestamp(date.getTime());
        try {
            stmt = con.prepareStatement("INSERT INTO user_chat_log VALUES (?,?,?,?,?)");
            stmt.setInt(1, userId);
            stmt.setString(2, userQuery);
            stmt.setString(3, botAnswer);
            stmt.setByte(4, helpful);
            stmt.setTimestamp(5, sqlTimeStamp);
            success = stmt.execute();
        } catch (Exception ex) {
            success = false;
        }
        closeConnection();
        return success;
    }

}
