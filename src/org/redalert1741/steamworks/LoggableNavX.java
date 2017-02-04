package org.redalert1741.steamworks;

import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI.Port;

public class LoggableNavX extends AHRS implements Loggable
{
	public LoggableNavX(Port spi_port_id)
	{
		super(spi_port_id);
	}

	@Override
	public void setupLogging(DataLogger logger)
	{
		logger.addAttribute("navx_Pitch");
		logger.addAttribute("navx_Roll");
		logger.addAttribute("navx_Yaw");
		logger.addAttribute("navx_XAcc");
		logger.addAttribute("navx_YAcc");
		logger.addAttribute("navx_ZAcc");
		logger.addAttribute("navx_Temp");
		logger.addAttribute("navx_Bar");
		logger.addAttribute("navx_Alt");
		logger.addAttribute("navx_XDis");
		logger.addAttribute("navx_YDis");
		logger.addAttribute("navx_YAcc");
	}

	@Override
	public void log(DataLogger logger)
	{
		logger.log("navx_Pitch", getPitch());
		logger.log("navx_Roll", getRoll());
		logger.log("navx_Yaw", getYaw());
		logger.log("navx_XAcc", getRawAccelX());
		logger.log("navx_YAcc", getRawAccelY());
		logger.log("navx_ZAcc", getRawAccelZ());
		logger.log("navx_Temp", getTempC());
		logger.log("navx_Bar", getBarometricPressure());
		logger.log("navx_Alt", getAltitude());
		logger.log("navx_XDis", getDisplacementX());
		logger.log("navx_YDis", getDisplacementY());
	}
}
