/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.edu.nus.iss.phoenix.scheduleprogram.service;

/**
 *
 * @author thushara
 */

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import sg.edu.nus.iss.phoenix.core.dao.DAOFactoryImpl;
import sg.edu.nus.iss.phoenix.core.exceptions.InvalidScheduleException;
import sg.edu.nus.iss.phoenix.core.exceptions.NotFoundException;
import sg.edu.nus.iss.phoenix.core.exceptions.OverLapException;
import sg.edu.nus.iss.phoenix.scheduleprogram.dao.ScheduleProgramDAO;
import sg.edu.nus.iss.phoenix.scheduleprogram.dao.WeeklyScheduleDAO;
import sg.edu.nus.iss.phoenix.scheduleprogram.entity.ProgramSlot;
import sg.edu.nus.iss.phoenix.scheduleprogram.entity.WeeklySchedule;
public class ScheduleService {
    DAOFactoryImpl factory;
	ScheduleProgramDAO spdao;
        WeeklyScheduleDAO weeklyScheduleDAO;
        public ScheduleService() {
		super();
		// Sorry. This implementation is wrong. To be fixed.
		factory = new DAOFactoryImpl();
		spdao = factory.getSpdao();
                weeklyScheduleDAO=factory.getWeeklyScheduleDAO();
	}
        
        public void processCreate(ProgramSlot ps) {
		try {
                    WeeklySchedule weeklySch= weeklyScheduleDAO.createValueObject();
                    weeklySch.setWeekNo(getWeekNumber(ps));
                    weeklySch.setStartDate(ps.getDateOfProgram()); // setting Date same as Program Start Date, it will be updated in WeeklyScheduleDAO
		if(!checkProgramSlotOverlap(ps, weeklySch))
                    spdao.create(ps);
		} catch (OverLapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NotFoundException e){
                    e.printStackTrace();
                }catch (SQLException e){
                    e.printStackTrace();
                }
	}
        
        
        public void processModify(ProgramSlot ps) {
		
			try {
                    WeeklySchedule weeklySch= weeklyScheduleDAO.createValueObject();
                    weeklySch.setWeekNo(getWeekNumber(ps));
                    weeklySch.setStartDate(ps.getDateOfProgram()); // setting Date same as Program Start Date, it will be updated in WeeklyScheduleDAO
		if(!checkProgramSlotOverlap(ps, weeklySch))
				spdao.save(ps);
		} catch (OverLapException e) {
		    e.printStackTrace();
		}catch (NotFoundException e){
                    e.printStackTrace();
                }catch (SQLException e){
                    e.printStackTrace();
                }
		
	}
        
        public ArrayList<ProgramSlot> findAllSP() {
		ArrayList<ProgramSlot> currentList = new ArrayList<ProgramSlot>();
		try {
			currentList = (ArrayList<ProgramSlot>) spdao.loadAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentList;


        }
        
        public int deteleProgramSlot(ProgramSlot programSlot) //throws InvalidScheduleException, NotFoundException, SQLException
        {   int result =0;
            if(programSlot==null){
               // throw new InvalidScheduleException("Input object is null");
            }
            if(programSlot.getStarttime()==null || !checkValidDateAndTime(programSlot.getDateOfProgram())){
               // throw new InvalidScheduleException("Input object start Time is null or incorrect");
            }
            if(programSlot.getDateOfProgram()==null || !checkValidDateAndTime(programSlot.getDateOfProgram())){
               // throw new InvalidScheduleException("Input object data of Program is null or incorrect");
            }
            if(programSlot.getProgamName()== null || programSlot.getProgamName().isEmpty()){
                //throw new InvalidScheduleException("Input program Name is null or empty");
            }
            try {
                result = spdao.delete(programSlot);
            }catch(NotFoundException nFE){
                
            }catch(SQLException sE){
                
            }
            return result;
        }
        
        protected boolean checkProgramSlotOverlap(ProgramSlot programSlot,WeeklySchedule weeklySch) throws NotFoundException, SQLException,OverLapException{
         weeklySch=  weeklyScheduleDAO.getWeeklySchedule(weeklySch);
         Date startDate = new Date(weeklySch.getStartDate().getTime());
         List<ProgramSlot> programSlotForWeek = spdao.loadAllProgramSlotForWeek(startDate);
         for(ProgramSlot ps:programSlotForWeek){
            if(programSlot.getDateOfProgram().compareTo(ps.getDateOfProgram())==0 && programSlot.getStarttime().compareTo(ps.getStarttime())==0){
                // Date of Program and Start Time already Present for the Week, throw OverlapException
                throw new OverLapException("Program Slot already assigned to Other Program");
            } 
         }   
         return false;
        }
        
        protected String getWeekNumber(ProgramSlot ps){
            Calendar cal = Calendar.getInstance();
        cal.setTime(ps.getStarttime());
        int weekNo = cal.get(Calendar.WEEK_OF_YEAR);
        return Integer.toString(weekNo);
        }
        
        protected boolean checkValidDateAndTime(java.util.Date date){
            Calendar cal = Calendar.getInstance();
            cal.setLenient(false);
            cal.setTime(date);
            cal.getTime();
            return true;
        }
}

