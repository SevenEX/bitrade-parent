package cn.ztuo.bitrade.util;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.config.JDBCConfig;
import cn.ztuo.bitrade.dto.MemberBonusDTO;
import cn.ztuo.bitrade.entity.GiftConfig;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.es.ESUtils;
import cn.ztuo.bitrade.service.GiftConfigService;
import cn.ztuo.bitrade.service.MemberWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * @Description:
 * @author: Seven
 * @date: create in 13:10 2018/7/2
 * @Modified:
 */
@Slf4j
@Component
public class JDBCUtils {

    @Autowired
    private JDBCConfig jdbcConfig;
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    @Autowired
    private ESUtils esUtils;

    @Autowired
    private GiftConfigService giftConfigService;

    @Autowired
    private MemberWalletService memberWalletService;


    public void batchJDBC(List<MemberBonusDTO> paramList) {
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO member_bonus ( member_id, have_time, arrive_time, mem_bouns, coin_id,total) VALUES ( ?, ?, ?, ?, ?, ?)";
        try {

            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            System.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");

            //Open a connection
            log.info("Connecting to a selected database...");
            conn = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            log.info("Connected database successfully...");


            stmt = conn.prepareStatement(sql);

            conn.setAutoCommit(false);
            for (int i = 0; i < paramList.size(); i++) {
                stmt.setLong(1, paramList.get(i).getMemberId());
                stmt.setString(2, paramList.get(i).getHaveTime());
                stmt.setString(3, paramList.get(i).getArriveTime());
                stmt.setBigDecimal(4, paramList.get(i).getMemBouns());
                stmt.setString(5, paramList.get(i).getCoinId());
                stmt.setBigDecimal(6, paramList.get(i).getTotal());

                stmt.addBatch();
                if (i % 2000 == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            log.info("Inserted records into the table...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("------批量操作SQL：" + sql);
        log.info("------执行时间：" + (endTime - startTime) + "ms");

    }

    /**
     * 糖果赠送
     * @param giftId
     */
    public void giftJDBC(Long giftId) {
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        Long dateStr = startTime;
        //查询赠送设置记录
        GiftConfig giftConfig = giftConfigService.findById(giftId);
        if (giftConfig == null){
            log.info("------该赠送配置不存在");
            return;
        }
        Integer createResult = memberWalletService.createGiftTable(dateStr,giftConfig.getHaveCoin());
        if (createResult == 0){
            log.info("------创建快照表失败");
            return;
        }

        List<MemberWallet> allList = memberWalletService.findGiftTable(dateStr);

        //新增用户赠送记录
        String sql = "INSERT INTO gift_record ( user_id, gift_name, gift_coin, gift_amount, create_time) VALUES ( " +
                "?, ?, ?, ?, ?)";
        BigDecimal sumAmount = memberWalletService.sumGiftTable(dateStr);
        //增加用户余额
        String updateSql = "UPDATE member_wallet SET balance=balance+? WHERE coin_id=? AND member_id=?";


        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            System.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            stmt = conn.prepareStatement(sql);
            updateStmt = conn.prepareStatement(updateSql);
            conn.setAutoCommit(false);
            String date = DateUtil.getDateTime();
            for (int i = 0; i < allList.size(); i++) {
                stmt.setLong(1, allList.get(i).getMemberId());
                stmt.setString(2, giftConfig.getGiftName());
                stmt.setString(3, giftConfig.getGiftCoin());
                BigDecimal memberBalance = allList.get(i).getBalance();
                //计算用户所占比例
                BigDecimal ratio = memberBalance.divide(sumAmount,4,BigDecimal.ROUND_DOWN);
                if (ratio.compareTo(BigDecimal.ZERO) == 0){
                    continue;
                }
                //计算用户增加数量
                BigDecimal addBalance = giftConfig.getAmount().multiply(ratio).setScale(4,BigDecimal.ROUND_DOWN);
                stmt.setBigDecimal(4, addBalance);
                stmt.setString(5, date);

                updateStmt.setBigDecimal(1,addBalance);
                updateStmt.setString(2,giftConfig.getGiftCoin());
                updateStmt.setLong(3,allList.get(i).getMemberId());

                stmt.addBatch();
                updateStmt.addBatch();
                if (i % 2000 == 0) {
                    stmt.executeBatch();
                    updateStmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            updateStmt.executeBatch();
            conn.commit();
            log.info("Inserted records into the table...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("------批量操作SQL：" + sql);
        log.info("------糖果赠送执行时间：" + (endTime - startTime) + "ms");

    }



    //分红到用户钱包
    public void batchJDBCUpdate(List<MemberWallet> paramList, BigDecimal BHBAmount, BigDecimal bounsAmount) {
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "UPDATE member_wallet SET balance=balance+? WHERE coin_id='Ethereum' AND member_id=?";
//        String sql = "UPDATE wealth_info SET bonus_amount=bonus_amount+?,release_amount=release_amount+? WHERE member_id=?";
        try {

            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            System.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");

            //Open a connection
            log.info("Connecting to a selected database...");
            conn = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            log.info("Connected database successfully...");

            stmt = conn.prepareStatement(sql);

            conn.setAutoCommit(false);
            for (int i = 0; i < paramList.size(); i++) {
                BigDecimal balance = paramList.get(i).getBalance();
                BigDecimal balanceAdd = balance.divide(BHBAmount, 8, BigDecimal.ROUND_DOWN).multiply(bounsAmount);
                stmt.setBigDecimal(1, balanceAdd);
//                stmt.setBigDecimal(2, balanceAdd);
                stmt.setLong(2, paramList.get(i).getMemberId());
                stmt.addBatch();
                if (i % 2000 == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            log.info("UPDATE records into the table...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            log.info(se + "========================");
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            log.info(e + "========================");
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("------批量操作SQL：" + sql);
        log.info("------执行时间：" + (endTime - startTime) + "ms");

    }




    /**
     * ES同步数据使用
     */
    public void dataSynchronization() {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            //1.获取Connection
            connection = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            //2.获取Statement
            statement = connection.createStatement();
            //3.准备Sql
            String sql1 = "SELECT * FROM exchange_order_transaction_mine WHERE id>=";
            String sql2 = " AND id<";
            int total = 2471348;
            int start = 22162;
            int end= 24162;
            for (int i = 1; i <= 1225; i++) {
                if ( end >= total){
                    end = total;
                }
                String sql = sql1+start+sql2+end;
                start = end;
                end =end+2000;
                log.info("====sql===="+sql);
                rs = statement.executeQuery(sql);

                JSONObject jsonObject = null;
                while(rs.next()){
                    //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                    jsonObject = new JSONObject();
                    jsonObject.put("id",rs.getLong("id"));
                    jsonObject.put("exchange_order_id",rs.getString("exchange_order_id"));
                    jsonObject.put("member_id",rs.getString("member_id"));
                    jsonObject.put("mine_amount",rs.getBigDecimal("mine_amount").doubleValue());
                    jsonObject.put("poundage_amount",rs.getBigDecimal("poundage_amount") == null ? 0 : rs.getBigDecimal("poundage_amount").doubleValue());
                    jsonObject.put("poundage_amount_Eth",rs.getBigDecimal("poundage_amount_Eth") == null ? 0 : rs.getBigDecimal("poundage_amount_Eth").doubleValue());
                    jsonObject.put("bouns_state",rs.getString("bouns_state"));
                    jsonObject.put("coin_id",rs.getString("coin_id"));
                    jsonObject.put("transaction_time", DateUtil.dateToString(rs.getDate("transaction_time")));
                    jsonObject.put("inviter_mobile",rs.getString("inviter_mobile") == null ? "": rs.getString("inviter_mobile"));
                    jsonObject.put("inviter_name",rs.getString("inviter_name") == null ? "" : rs.getString("inviter_name") );
                    jsonObject.put("type",rs.getString("type"));
                    jsonObject.put("symbol",rs.getString("symbol"));
                    jsonObject.put("direction",rs.getString("direction"));
                    jsonObject.put("inviter_state",rs.getString("inviter_state"));
                    log.info("===存入ES 数据==="+jsonObject);
                    boolean result = esUtils.save(jsonObject);
                    log.info("====result===="+result);
                    if ( result){
                        log.info("====存入ES成功====");
                    }else {
                        log.info("====存入ES失败====");
                    }
                    Thread.sleep(10);

                }

            }
            //5.处理ResultSet

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (statement != null) {
                    connection.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    /**
     * 同步交易明细记录
     */
    public void dataSynchronization2Membertransaction() {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            //1.获取Connection
            connection = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            //2.获取Statement
            statement = connection.createStatement();
            //3.准备Sql
            String sql1 = "SELECT * FROM member_transaction WHERE id>=";
            String sql2 = " AND id<";
            int total = 10058359;
            int start = 1;
            int end= 2000;
            for (int i = 1; i <= 5030; i++) {
                if ( end >= total){
                    end = total;
                }
                String sql = sql1+start+sql2+end;
                start = end;
                end =end+2000;
                log.info("====sql===="+sql);
                rs = statement.executeQuery(sql);

                JSONObject jsonObject = null;
                while(rs.next()){
                    //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                    jsonObject = new JSONObject();
                    jsonObject.put("id",rs.getLong("id"));
                    jsonObject.put("address",rs.getString("address"));
                    jsonObject.put("member_id",rs.getString("member_id"));
                    jsonObject.put("amount",rs.getBigDecimal("amount").doubleValue());
                    jsonObject.put("create_time",DateUtil.dateToString(rs.getDate("create_time")));
                    jsonObject.put("fee",rs.getBigDecimal("fee").doubleValue());
                    jsonObject.put("flag",rs.getString("flag"));
                    jsonObject.put("symbol", rs.getString("symbol"));
                    jsonObject.put("real_fee",rs.getString("real_fee"));
                    jsonObject.put("discount_fee",rs.getString("discount_fee"));
                    jsonObject.put("type",rs.getString("type"));
                    log.info("===存入ES 数据==="+jsonObject);
                    boolean result = esUtils.saveForAnyOne(jsonObject,"member_transaction","mem_transaction");
                    log.info("====result===="+result);
                    if ( result){
                        log.info("====存入ES成功====");
                    }else {
                        log.info("====存入ES失败====");
                    }
                    Thread.sleep(10);

                }
                log.info(">>>>>>此次插入时间>>>>"+(System.currentTimeMillis()-startTime));
            }
            //5.处理ResultSet

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (statement != null) {
                    connection.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    /**
     * 初始化 wealthInfo 表
     * @param members
     */
    public void dataSynchronization2MemberWealthInfo(List<Member> members){
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO wealth_info ( member_id, member_name, member_mobile, inviter_id, member_rate,give_bhb,accumulated_mine," +
                "release_rate,fee_amount,promotion_time,over_time,release_amount,bonus_amount) VALUES ( ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?,?)";
        try {

            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            System.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");

            //Open a connection
            log.info("Connecting to a selected database...");
            conn = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            log.info("Connected database successfully...");


            stmt = conn.prepareStatement(sql);

            conn.setAutoCommit(false);
            for (int i = 0; i < members.size(); i++) {
                stmt.setLong(1, members.get(i).getId());
                stmt.setString(2, members.get(i).getRealName());
                stmt.setString(3, members.get(i).getMobilePhone());
                stmt.setString(4, members.get(i).getInviterId()==null?null:members.get(i).getInviterId().toString());
                stmt.setString(5, "0");
                stmt.setString(6, "10000");
                stmt.setString(7, "0");
                stmt.setString(8, "1");
                stmt.setString(9, "0");
                stmt.setString(10, DateUtil.getDateTime());
                stmt.setString(11, DateUtil.getDateTime());
                stmt.setString(12, "0");
                stmt.setString(13, "0");

                stmt.addBatch();
                if (i % 2000 == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            log.info("Inserted records into the table...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("------批量操作SQL：" + sql);
        log.info("------执行时间：" + (endTime - startTime) + "ms");

    }

    public void deleteFromMemberByRelaNameStatus() {
        long startTime = System.currentTimeMillis();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Statement updateStatement = null;
        try {
            //1.获取Connection
            connection = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            //2.获取Statement
            statement = connection.createStatement();
            updateStatement = connection.createStatement();
            //3.准备Sql
            String sql = " SELECT  inviter_id, count(inviter_id) counts from member WHERE inviter_id is not null and id_number is NULL GROUP BY inviter_id ";

            rs = statement.executeQuery(sql);

            while(rs.next()){
                Long memberId = rs.getLong("inviter_id");
                Long counts = rs.getLong("counts");
                String updateSql = "update member_wallet set to_released=to_released - "+(counts*60 )+" where member_id = "+memberId+ " AND coin_id='BHB' ";
                log.info(">>>>>>更新sql>>>>>"+updateSql);
                updateStatement.executeUpdate(updateSql);
                log.info(">>>>此次更新数据会员id>>"+memberId+">>>金额>>>>"+(counts*60)+">>>>清理会员>>>"+counts);
                log.info("会员:"+memberId+"名下有:"+counts+"被邀请人未实名，扣减BHB数量:"+(counts*60));
                Thread.sleep(10);

            }
            log.info(">>>>>>此次插入时间>>>>"+(System.currentTimeMillis()-startTime));
        //5.处理ResultSet

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (statement != null) {
                    connection.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void synchronization2MemberRegisterWallet(List<Member> members,String coinId) {
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;
        Statement state = null;
        ResultSet rs = null;
        String sql = " INSERT INTO member_wallet( address, balance, frozen_balance, is_lock, member_id, version, coin_id, to_released )" +
                " VALUES ( \"\", 0, 0, 0, ?, 0, ?, 0) ";

        try {

            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            System.setProperty("jdbc.driver", "com.mysql.jdbc.Driver");

            //Open a connection
            log.info("Connecting to a selected database...");
            conn = DriverManager.getConnection(jdbcConfig.getDbURRL(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
            log.info("Connected database successfully...");


            stmt = conn.prepareStatement(sql);

            state = conn.createStatement();
            String querySql = "select id from member";
            rs = state.executeQuery(querySql);
            conn.setAutoCommit(false);

            int i=0;
            while(rs.next()){

                log.info("sql>>>>"+sql);
                log.info("会员id>>>>>"+rs.getLong("id")+">>>>币种>>>"+coinId);
                stmt.setLong(1, rs.getLong("id"));
                stmt.setString(2, coinId);
                i++;
                stmt.addBatch();
                if (i % 2000 == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            log.info("Inserted records into the table...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }

            } catch (SQLException se) {
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("------批量操作SQL：" + sql);
        log.info("------执行时间：" + (endTime - startTime) + "ms");

    }

//    public static void main(String[] args) {
//        List<MemberWallet> memberWallets = new ArrayList<>();
//         String sql1 = "SELECT * FROM member_transaction WHERE id>=";
//        String sql2 = " AND id<";
//        int total = 10058359;
//        int start = 1;
//        int end= 2000;
//        for (int i = 1; i <= 5030; i++) {
//            if ( end >= total){
//                end = total;
//            }
//            String sql = sql1+start+sql2+end;
//            start = end;
//            end =end+2000;
//            System.out.println(sql);
//        }
//
//    }


}
