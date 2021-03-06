/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl;

/**
 *
 * @author thushara
 */
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import sg.edu.nus.iss.phoenix.core.dao.DBConstants;
import sg.edu.nus.iss.phoenix.core.exceptions.NotFoundException;
import sg.edu.nus.iss.phoenix.radioprogram.entity.RadioProgram;
import sg.edu.nus.iss.phoenix.scheduleprogram.dao.ScheduleProgramDAO;
import sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot;

/**
 * ScheduleProgram Data Access Object (DAO). This class contains all database
 * handling that is needed to permanently store and retrieve ScheduleProgram object
 * instances.
 */

public class ScheduleDAOImpl implements ScheduleProgramDAO{
    
    Connection connection;
    
    /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#createValueObject()
	 */
    
    @Override
	public ProgramSlot createValueObject() {
		return new ProgramSlot();
	}
        
        /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#getObject(java.lang.String)
	 */
        
    @Override
	public ProgramSlot getObject(String name, Date Progdate,int duration) throws NotFoundException,
			SQLException {
    
		ProgramSlot valueObject = createValueObject();
		RadioProgram rp = new RadioProgram();
                valueObject.setRadioProgram(rp);
                valueObject.getRadioProgram().setName(name);
                valueObject.setDuration(duration);
                valueObject.setDateOfProgram(Progdate);
		load(valueObject);
		return valueObject;
	}
        
        /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#load(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
	 */
    
        @Override
	public void load(ProgramSlot valueObject) throws NotFoundException,
			SQLException {

		if (valueObject.getRadioProgram().getName()== null || valueObject.getRadioProgram().getName().trim().isEmpty()) {
			// System.out.println("Can not select without Primary-Key!");
			throw new NotFoundException("Can not select without Primary-Key!");
		}

		String sql = "SELECT * FROM phoenix.`program-slot`; WHERE (`program-name` = ? ); ";
		PreparedStatement stmt = null;
		openConnection();
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, valueObject.getRadioProgram().getName());

			singleQuery(stmt, valueObject);

		} finally {
			if (stmt != null)
				stmt.close();
			closeConnection();
		}
	}
        
        /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#loadAll()
	 */
        
        @Override
	public List<ProgramSlot> loadAll() throws SQLException {
		openConnection();
		String sql = "SELECT * FROM `program-slot` ORDER BY `program-name` ASC; ";
		List<ProgramSlot> searchResults = listQuery(connection
				.prepareStatement(sql));
		closeConnection();
		
		return searchResults;
	}
        
        /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#create(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
	 */
        
        @Override
	public synchronized void create(ProgramSlot valueObject)
			throws SQLException {

		String sql = "";
		PreparedStatement stmt = null;
		openConnection();
		try {
			sql = "INSERT INTO `program-slot` (`program-name`, `dateOfProgram`, `startTime`,`duration`,`presenter`, `producer` ) VALUES (?,?,?,?,?,?); ";
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, valueObject.getRadioProgram().getName());
			stmt.setDate(2, new java.sql.Date(valueObject.getDateOfProgram().getTime()));
			stmt.setTime(3, valueObject.getStartTime());
                        stmt.setInt(4, valueObject.getDuration());
                        stmt.setString(5, valueObject.getPresenter());
                        stmt.setString(6, valueObject.getProducer());
			int rowcount = databaseUpdate(stmt);
			if (rowcount != 1) {
				// System.out.println("PrimaryKey Error when updating DB!");
				throw new SQLException("PrimaryKey Error when updating DB!");
			}

		} finally {
			if (stmt != null)
				stmt.close();
			closeConnection();
		}

	}
        
     /**
	 * databaseUpdate-method. This method is a helper method for internal use.
	 * It will execute all database handling that will change the information in
	 * tables. SELECT queries will not be executed here however. The return
	 * value indicates how many rows were affected. This method will also make
	 * sure that if cache is used, it will reset when data changes.
	 * 
	 * @param stmt
	 *            This parameter contains the SQL statement to be executed.
         * @return 
         * @throws java.sql.SQLException
     */
        
    protected int databaseUpdate(PreparedStatement stmt) throws SQLException {

		int result = stmt.executeUpdate();

		return result;
	}
    
        /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#save(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
	 */
        
        @Override
	public void save(ProgramSlot valueObject) throws NotFoundException,
			SQLException {

		String sql = "UPDATE `program-slot` SET `startTime`=?, presenter =?, producer =?, duration =?, dateOfProgram =?  WHERE (id = ? ); ";
		PreparedStatement stmt = null;
		openConnection();
		try {
			stmt = connection.prepareStatement(sql);
			
			stmt.setTime(1, valueObject.getStartTime());
                        stmt.setString(2, valueObject.getPresenter());
                        stmt.setString(3, valueObject.getProducer());
                        stmt.setInt(4, valueObject.getDuration());
                        stmt.setDate(5, new java.sql.Date(valueObject.getDateOfProgram().getTime()));
                        stmt.setInt(6, valueObject.getId());
                       // stmt.setInt(7, valueObject.getId());

			int rowcount = databaseUpdate(stmt);
			if (rowcount == 0) {
				// System.out.println("Object could not be saved! (PrimaryKey not found)");
				throw new NotFoundException(
						"Object could not be saved! (PrimaryKey not found)");
			}
			if (rowcount > 1) {
				// System.out.println("PrimaryKey Error when updating DB! (Many objects were affected!)");
				throw new SQLException(
						"PrimaryKey Error when updating DB! (Many objects were affected!)");
			}
		} finally {
			if (stmt != null)
				stmt.close();
			closeConnection();
		}
	}

        /**
	 * databaseQuery-method. This method is a helper method for internal use. It
	 * will execute all database queries that will return only one row. The
	 * result set will be converted to valueObject. If no rows were found,
	 * NotFoundException will be thrown.
	 * 
	 * @param stmt
	 *            This parameter contains the SQL statement to be executed.
	 * @param valueObject
	 *            Class-instance where resulting data will be stored.
     * @throws sg.edu.nus.iss.phoenix.core.exceptions.NotFoundException
     * @throws java.sql.SQLException
	 */
        	        
        protected void singleQuery(PreparedStatement stmt, ProgramSlot valueObject)
			throws NotFoundException, SQLException {

		ResultSet result = null;
		openConnection();
		try {
			result = stmt.executeQuery();

			if (result.next()) {

				RadioProgram rp = new RadioProgram();
                                valueObject.setRadioProgram(rp);
                                valueObject.setId(result.getInt("id"));
                                valueObject.getRadioProgram().setName(result.getString("program-name"));
				valueObject.setDuration(result.getInt("duration"));
				valueObject.setDateOfProgram(result.getDate("dateOfProgram"));
                              valueObject.setStartTime(result.getTime("startTime"));

			} else {
				// System.out.println("RadioProgram Object Not Found!");
				throw new NotFoundException("ProgramSlot Object Not Found!");
			}
		} finally {
			if (result != null)
				result.close();
			if (stmt != null)
				stmt.close();
			closeConnection();
		}
	}
        
       /**
	 * databaseQuery-method. This method is a helper method for internal use. It
	 * will execute all database queries that will return multiple rows. The
	 * result set will be converted to the List of valueObjects. If no rows were
	 * found, an empty List will be returned.
	 * 
	 * @param stmt
	 *            This parameter contains the SQL statement to be executed.
     * @return 
     * @throws java.sql.SQLException
	 */
        
        protected List<ProgramSlot> listQuery(PreparedStatement stmt) throws SQLException {

		ArrayList<ProgramSlot> searchResults = new ArrayList<>();
		ResultSet result = null;
		openConnection();
		try {
			result = stmt.executeQuery();

			while (result.next()) {
				ProgramSlot temp = createValueObject();
                                RadioProgram rp = new RadioProgram();
                                temp.setRadioProgram(rp);
                                temp.setId(result.getInt("id"));
                                temp.getRadioProgram().setName(result.getString("program-name"));
				temp.setDateOfProgram(new java.util.Date(result.getDate("dateOfProgram").getTime()));
				temp.setStartTime(result.getTime("startTime"));
                                temp.setDuration(result.getInt("duration"));
                                temp.setPresenter(result.getString("presenter"));
                                temp.setProducer(result.getString("producer"));
				searchResults.add(temp);
			}

		} finally {
			if (result != null)
				result.close();
			if (stmt != null)
				stmt.close();
			closeConnection();
		}

		return (List<ProgramSlot>) searchResults;
	}

	private void openConnection() {
		try {
			Class.forName(DBConstants.COM_MYSQL_JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.connection = DriverManager.getConnection(DBConstants.dbUrl,
					DBConstants.dbUserName, DBConstants.dbPassword);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
        private void closeConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /* (non-Javadoc)
     * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#searchMatching(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
     */
        
    @Override
    public List<ProgramSlot> searchMatching(ProgramSlot valueObject) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /* (non-Javadoc)
	 * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#deleteAll(java.sql.Connection)
	 */
    
    @Override
    public void deleteAll(Connection conn) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     /* (non-Javadoc)
     * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#loadAllProgramSlotForWeek(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
     */
    @Override
    public List<ProgramSlot> loadAllProgramSlotForWeek(Date weekStartDate) throws SQLException {
                Calendar cal =Calendar.getInstance();
                cal.setTime(weekStartDate);
                cal.add(Calendar.DAY_OF_YEAR, 7);
                
                Date weekEndDate= new Date(cal.getTime().getTime());
                openConnection();
		String sql = "SELECT * FROM `program-slot` WHERE(dateOfProgram between ? and ? ) ORDER BY `dateOfProgram` ASC, `startTime` ASC ; ";
		PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setDate(1, weekStartDate);
                stmt.setDate(2, weekEndDate);
                List<ProgramSlot> searchResults = listQuery(stmt);
		closeConnection();
		return searchResults;
    }
    
    /* (non-Javadoc)
     * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#updatePresenterProducer(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
     */

    @Override
    public void updatePresenterProducer(ProgramSlot valueObject) throws SQLException {
        String presenterSql = "UPDATE `program-slot` SET `presenter`= NULL WHERE (`presenter` = ?); ";
	PreparedStatement stmtPresenter = null;
        String producerSql = "UPDATE `program-slot` SET `producer`= NULL WHERE (`producer` = ?); ";
	PreparedStatement stmtProducer = null;
		openConnection();
		try {
			stmtPresenter = connection.prepareStatement(presenterSql);
			stmtProducer = connection.prepareStatement(producerSql);
			stmtPresenter.setString(1, valueObject.getPresenter());
                        stmtProducer.setString(1, valueObject.getProducer());

			int rowcount = databaseUpdate(stmtPresenter);
                        int rowcount_producer = databaseUpdate(stmtProducer);
			
		} finally {
			if (stmtPresenter != null)
                            stmtPresenter.close();
                        if(stmtProducer!=null)
                            stmtProducer.close();
			closeConnection();
		}
    }
    
     /* (non-Javadoc)
     * @see sg.edu.nus.iss.phoenix.scheduleprogram.dao.impl.ScheduleProgramDAO#delete(sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot)
     */

    @Override
    public void delete(ProgramSlot valueObject) throws NotFoundException, SQLException {
        String sql = "DELETE FROM `program-slot` WHERE (`startTime`=? AND `dateOfProgram`=?  AND `program-name` = ?); ";
		PreparedStatement stmt = null;
		openConnection();
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setTime(1, valueObject.getStartTime());
                        stmt.setDate(2, new java.sql.Date(valueObject.getDateOfProgram().getTime()));
                        stmt.setString(3, valueObject.getRadioProgram().getName());

			int rowcount = databaseUpdate(stmt);
			if (rowcount == 0) {
				// System.out.println("Object could not be saved! (PrimaryKey not found)");
				throw new NotFoundException(
						"Object could not be deleted! (Programslot not found)");
			}
			if (rowcount > 1) {
				// System.out.println("PrimaryKey Error when updating DB! (Many objects were affected!)");
				throw new SQLException(
						"PrimaryKey Error when deleting program slot from DB! (Many objects were affected!)");
			}
      
		} finally {
			if (stmt != null)
				stmt.close();
                                closeConnection();
		}
    }
}
