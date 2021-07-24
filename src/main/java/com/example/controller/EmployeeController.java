package com.example.controller;

import com.example.exception.ResourceNotAvailableException;
import com.example.model.BookingDetails;
import com.example.model.Employee;
import com.example.model.PatientDetails;
import com.example.model.User;
import com.example.registration.repository.BookingDetailsRepository;
import com.example.registration.repository.PatientDetailsRepository;
import com.example.registration.service.NotificationService;
import com.example.repository.EmployeeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@RestController
@RequestMapping("/api")
public class EmployeeController {

	private Logger logger = LoggerFactory.getLogger(EmployeeController.class);
	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	@Qualifier("patientDetailsRepoClass")
	private PatientDetailsRepository patientDetailsRepository;
	@Autowired
	private BookingDetailsRepository bookingDetailsRepository;

	
	@Autowired
	private NotificationService notificationService;
	// get all employees
	@GetMapping("/employees")
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	//creating employee
	@PostMapping("/createEmployee")
	public Employee createEmployee(@RequestBody Employee employee) {
		return employeeRepository.save(employee);

	}

	//creating patient
	@PostMapping("/createPatient")
	public int createPatient(@RequestBody PatientDetails patientDetails) {
		return patientDetailsRepository.save(patientDetails);

	}

	@GetMapping("/getAllPatients")
	public List<PatientDetails> patientDetailsList() {
		return patientDetailsRepository.findAll();
	}

	@GetMapping("/getPatient/{doctorName}")
	public List<PatientDetails> patientDetailsListForDoctor(@PathVariable String doctorName) {
		return patientDetailsRepository.findPatientDetailsListForDoctor(doctorName);
	}

	//get employee by id rest api
	@GetMapping("/employees/{id}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotAvailableException("Employee  does not exist with id" + id));
		return ResponseEntity.ok(employee);
	}

	//update employee
	@PutMapping("/employees/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotAvailableException("Employee  does not exist with id" + id));
		employee.setEmailId(employeeDetails.getEmailId());
		employee.setMobileNumber(employeeDetails.getMobileNumber());
		employee.setSpecialization(employeeDetails.getSpecialization());
		employee.setRole(employeeDetails.getRole());
		employee.setGender(employeeDetails.getGender());
		employee.setUserName(employeeDetails.getUserName());
		employee.setPassword(employeeDetails.getPassword());

		Employee updatedEmployee = employeeRepository.save(employee);
		return ResponseEntity.ok(updatedEmployee);
	}

	//delete employee
	@DeleteMapping("/employees/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteEmployee(@PathVariable Long id) {
		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotAvailableException("Employee  does not exist with id" + id));

		employeeRepository.delete(employee);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return ResponseEntity.ok(response);
	}

	//creating patient
	@PostMapping("/createBooking")
	public BookingDetails createBooking(@RequestBody BookingDetails bookingDetails) {
		return bookingDetailsRepository.save(bookingDetails);
	}

	@GetMapping("/bookingInfo/{patientName}")
	public List<BookingDetails> retreivePatientBookingDetails(@PathVariable String patientName) {
		return bookingDetailsRepository.findByUserName(patientName);
	}

	@GetMapping("/bookingInfo/{reportingDoctor}/{specialization}")
	public List<BookingDetails> retreivePatientBookingForDoctor(@PathVariable String reportingDoctor, @PathVariable String specialization) {
		return bookingDetailsRepository.findByReportingDoctorAndSpecialization(reportingDoctor, specialization);
	}

	@GetMapping("/bookingInfo")
	public List<BookingDetails> retreivePatientBookingInfo() {
		return bookingDetailsRepository.findAll();
	}

	@GetMapping("/bookingInfoByDate/{reportingDoctor}/{specialization}")
	public List<BookingDetails> retreivePatientBookingInfo(@PathVariable String reportingDoctor, @PathVariable String specialization, @RequestParam String dateOfBooking) throws ParseException {
		Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBooking);
		return bookingDetailsRepository.findByReportingDoctorAndSpecializationAndDateOfAppointment(reportingDoctor, specialization, date1);
	}
	
	
	private void sendmail(User user) throws AddressException, MessagingException, IOException { 
		   Properties props = new Properties();
		   props.put("mail.smtp.auth", "true");
		   props.put("mail.smtp.starttls.enable", "true");
		   props.put("mail.smtp.host", "smtp.gmail.com");
		   props.put("mail.smtp.port", "587");
		   
		   Session session = Session.getInstance(props, new javax.mail.Authenticator() {
		      protected PasswordAuthentication getPasswordAuthentication() {
		         return new PasswordAuthentication("starsproject2021@gmail.com", "Lakumarapu1995!");
		      }
		   });
		   Message msg = new MimeMessage(session);
		   msg.setFrom(new InternetAddress("starsproject2021@gmail.com", false));

		   msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("l.kunni@gmail.com"));
		   msg.setSubject("Tutorials point email");
		   msg.setContent("Your appointment has been confirmed successfully", "text/html");
		   msg.setSentDate(new Date());

		   MimeBodyPart messageBodyPart = new MimeBodyPart();
		   messageBodyPart.setContent("Tutorials point email", "text/html");

		   Multipart multipart = new MimeMultipart();
		   multipart.addBodyPart(messageBodyPart);
		   msg.setContent(multipart);
		   Transport.send(msg);   
		}
	
	
	@RequestMapping(value = "/sendemail")
	public String sendEmail() throws AddressException, MessagingException, IOException {
		User user = new User();
		user.setName("santhu");
		user.setEmail("l.kunni@gmail.com");
	   sendmail(user);
	   return "Email sent successfully";   
	}
}
