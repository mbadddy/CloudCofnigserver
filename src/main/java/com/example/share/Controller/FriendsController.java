package com.example.share.Controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.share.Controller.DTOS.GroupDTO;
import com.example.share.Controller.DTOS.MessageDTO;
import com.example.share.Controller.DTOS.StudentDTO;
import com.example.share.Entities.Course;
import com.example.share.Entities.FileAploadUtil;
import com.example.share.Entities.Files;
import com.example.share.Entities.Groups;
import com.example.share.Entities.Messages;
import com.example.share.Entities.Requests;
import com.example.share.Entities.Roles;
import com.example.share.Entities.Student;
import com.example.share.Repositories.MessageRepo;
import com.example.share.Repositories.StudentRepostry;
import com.example.share.Services.Implement.FileServiceImple2;
import com.example.share.Services.Implement.StudentsServices;

@Controller
public class FriendsController {
	@Autowired
	private FileServiceImple2 services;
	@Autowired
	private MessageRepo msrrepo;
	@Autowired
	private StudentRepostry stdrepol;
	@ModelAttribute("messag")
	public MessageDTO obtainStudent() {
		return new MessageDTO();
	}
	@Autowired
	private StudentsServices service;
	@RequestMapping(value = "/chating")
	public String friendsChat(@RequestParam(value ="toid") long id,Model m,Authentication auth,HttpServletRequest request) {
		Student student=service.findStudent(id);
        Principal userPrincipal = request.getUserPrincipal();
		Student mystd=stdrepol.findByEmail(userPrincipal.getName());
		Collection<Student> whosendMetxt=service.whoSendMsgToMe(mystd.getId(),0,0);
		Collection<Messages> msgs=service.getAllMessagesReceived(mystd.getId(), 0,0,0);
	   for (Messages msgsss : student.getMsgfrom()) {
			if(msgsss.getStdTo().getId()==mystd.getId()) {
				System.out.println("msg id is "+id);
				System.out.println("msg is "+msgsss.getMsg());
				msgsss.setSeen(1);
				msrrepo.save(msgsss);
			}else {
				System.out.println("amna msg ");
			}
		}
	    m.addAttribute("tmsgs", msgs);
		m.addAttribute("tmsg", msgs.size());
		m.addAttribute("whosendMetxt",whosendMetxt);
		Collection<Messages> sent=service.getSentMsgs(mystd.getId(),student.getId(),0,0);
		Collection<Messages> sendby=service.getSendBy(student.getId(),mystd.getId(),0,0);
		Collection<Messages> receivOrsent=service.allreciivOrSent(mystd.getId());
		m.addAttribute("allsentRec", receivOrsent);
		m.addAttribute("totConver",(sendby.size()+sent.size()));
		for (Messages messages : sendby) {
			m.addAttribute("mess", messages.getId());
			System.out.println("msgid is "+messages.getId());
		}
		for (Student std : whosendMetxt) {
			m.addAttribute("toidd", std.getId());
			System.out.println("fromid is "+std.getId());
		}
		if(!sent.isEmpty()) {
			m.addAttribute("sent", sent);
		}
		
		m.addAttribute("namee", student.getFirst()+" "+student.getLast());
		m.addAttribute("agee", student.getAge());
		int yr=0;
		for (Course cs : student.getCourses()) {
			yr=cs.getYear();
		}
		m.addAttribute("yearr",yr);
		m.addAttribute("id",student.getId());
		m.addAttribute("university", student.getProgramme().getUniversity().getUn_name()+","+student.getProgramme().getCollege().getColl_name());
		m.addAttribute("couse", student.getCourses().size()+" courses");
		m.addAttribute("email", student.getEmail());
		m.addAttribute("prog", student.getProgramme().getProg_name());
		
		m.addAttribute("userimage", mystd.getPhotosImagePath());
		m.addAttribute("first", mystd.getFirst());
		m.addAttribute("last", mystd.getLast());
		m.addAttribute("sendby", sendby);
		Messages message=new Messages();
		m.addAttribute("message", message);
		m.addAttribute("sents", sent.size());
		System.out.println("name is "+student.getFirst());

		m.addAttribute("toid", student.getId());
		m.addAttribute("from", student.getFirst()+" "+student.getLast());
		m.addAttribute("picha", mystd.getPhotosImagePath());
		m.addAttribute("picha2", student.getPhotosImagePath());
		m.addAttribute("too", student.getId());
		Collection<Student> except=service.selectBlockedFriends(userPrincipal.getName(),0,mystd.getId());
	  if(except.contains(student)) {
			System.out.println("ipooooo");
			m.addAttribute("ipo",except); 
		}
		LocalTime localTime=LocalTime.now(ZoneId.of("GMT+02:59"));
		System.out.println("time is "+localTime.getHour()+":"+localTime.getMinute());	
		return "mychat";

	}
	@GetMapping("/chat")
	public String getChat(Model m,Authentication auth,HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=stdrepol.findByEmail(userPrincipal.getName());
		Collection<Student> whosendMetxt=service.whoSendMsgToMe(mystd.getId(),0,0);
		Collection<Messages> msgss=service.getAllMessagesReceived(mystd.getId(), 0,0,0);
		m.addAttribute("tmsgs", msgss);
		m.addAttribute("tmsg", msgss.size());
		m.addAttribute("whosendMetxt",whosendMetxt);
		m.addAttribute("userimage", mystd.getPhotosImagePath());
		m.addAttribute("first", mystd.getFirst());
		m.addAttribute("last", mystd.getLast());

		Messages message=new Messages();
		m.addAttribute("message", message);
		return "mychat";	
	}

	@RequestMapping(value = "/more")
	public String moreDetails(@RequestParam(value ="more") long id,Model m,
			RedirectAttributes redirect) {
		Student student=service.findStudent(id);
		Collection<Files> pubfiles=services.getFileHistory(3, student.getId());
		Collection<Files> taggedfiles=services.getFileHistory(5, student.getId());
		int yr = 0,sem = 0;
		for (Course cs : student.getCourses()) {
			yr=cs.getYear();
			sem=cs.getSemister();

		}

		if (student!=null) {
			redirect.addFlashAttribute("anu","1");
		}
		Collection<Student> frindsss=service.selectNewFriends(student.getEmail(), 0,student.getId());
		redirect.addFlashAttribute("name",student.getFirst()+" "+student.getLast());
		redirect.addFlashAttribute("age",student.getAge()+" yrs");
		redirect.addFlashAttribute("year",yr);
		redirect.addFlashAttribute("email",student.getEmail());
		redirect.addFlashAttribute("closee",frindsss);
		redirect.addFlashAttribute("photo",student.getPhotosImagePath());
		redirect.addFlashAttribute("sem",sem);
		redirect.addFlashAttribute("share",pubfiles.size()+" public files, "+taggedfiles.size()+" tagged files");
		redirect.addFlashAttribute("couse",student.getCourses().size()+" Courses");
		redirect.addFlashAttribute("progr",student.getProgramme().getProg_name());
		redirect.addFlashAttribute("University",student.getProgramme().getUniversity().getUn_name()+", "+student.getProgramme().getCollege().getColl_name());
		
		return "redirect:/friends";

	}
	@ModelAttribute("group")
	public GroupDTO obtainGroups() {
		return new GroupDTO();
	}
	@PostMapping("/savegrp")
	public String registerGroup(@Valid
			@ModelAttribute("group") GroupDTO groupDTO,BindingResult bindingResult
			,RedirectAttributes attributes,Authentication auth,HttpServletRequest request, @RequestParam("image") MultipartFile file
			) throws IOException  {
		Principal userPrincipal = request.getUserPrincipal();
		 Student mystd=stdrepol.findByEmail(userPrincipal.getName());
			String filename=org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
			groupDTO.setGrp_icon(filename);
			 Groups saveGroup=service.saveGroup(groupDTO, mystd);
			String uploadDir="src/main/resources/static/img1/userphotos/"+saveGroup.getId();
			FileAploadUtil.saveFile(uploadDir, filename, file);
		    System.out.println(" group success");
		    attributes.addFlashAttribute("messagee", "group created saccessfully");
			return "redirect:/friends";
		
	}

	@RequestMapping(value = "/moreD")
	public String moreReqDetails(@RequestParam(value ="morer") long id,
			RedirectAttributes redirect) {
		Student student=service.findStudent(id);
		Collection<Files> pubfiles=services.getFileHistory(3, student.getId());
		Collection<Files> taggedfiles=services.getFileHistory(5, student.getId());
		int yr = 0,sem = 0;
		for (Course cs : student.getCourses()) {
			yr=cs.getYear();
			sem=cs.getSemister();

		}

		if (student!=null) {
			redirect.addFlashAttribute("anuu","1");
		}
		Collection<Student> frindsss=service.selectNewFriends(student.getEmail(), 0,student.getId());
		redirect.addFlashAttribute("name",student.getFirst()+" "+student.getLast());
		redirect.addFlashAttribute("age",student.getAge()+" yrs");
		redirect.addFlashAttribute("year",yr);
		redirect.addFlashAttribute("email",student.getEmail());
		redirect.addFlashAttribute("closee",frindsss);
		redirect.addFlashAttribute("photo",student.getPhotosImagePath());
		redirect.addFlashAttribute("sem",sem);
		redirect.addFlashAttribute("share",pubfiles.size()+" public files, "+taggedfiles.size()+" tagged files");
		redirect.addFlashAttribute("couse",student.getCourses().size()+" Courses");
		redirect.addFlashAttribute("progr",student.getProgramme().getProg_name());
		redirect.addFlashAttribute("University",student.getProgramme().getUniversity().getUn_name()+", "+student.getProgramme().getCollege().getColl_name());
        return "redirect:/friends";

	}


	@PostMapping("/msgreq")
	public String sandMsg(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("toid") long msgid,@ModelAttribute("messag") MessageDTO msg) {
		Principal userPrincipal = request.getUserPrincipal();
		System.out.println("imefika apa");
		Student student=service.findStudent(msgid);
		System.out.println("imefika");
		System.out.println("to "+student);
		Student sd=service.saveMessage(msg,userPrincipal.getName(),msgid);
		redirect.addFlashAttribute("toid",msgid);
		if(sd!=null) {
			System.out.println("success");
			redirect.addFlashAttribute("messagee", "message sent to "+student.getFirst());
		}
		return "redirect:/chat";
	}
	@PostMapping("/achieve")
	public String achieveMsg(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("toid") long msgid) {
		Principal userPrincipal = request.getUserPrincipal();
		Student student=service.findStudent(msgid);
       Student mystd=stdrepol.findByEmail(userPrincipal.getName());
       Collection<Messages> sent=service.getSentMsgs(mystd.getId(),student.getId(),0,0);
		Collection<Messages> sendby=service.getSendBy(student.getId(),mystd.getId(),0,0);
		if(!sendby.isEmpty() || !sent.isEmpty()) {
			Collection<Messages> msgs=service.updateArchieve(mystd.getId(), student.getId(),0);
			if(msgs!=null) {
				System.out.println("success");
				redirect.addFlashAttribute("messagee", "message achieved");
				redirect.addFlashAttribute("mojaa", "1");
			}
			else {
				System.out.println("error");
				redirect.addFlashAttribute("messagee", "message already achieved previous time");
				redirect.addFlashAttribute("moja", "1");
			}
		}
		else {
			System.out.println("error");
			redirect.addFlashAttribute("messagee", "message already achieved previous time");
			redirect.addFlashAttribute("moja", "1");
		}
     	return "redirect:/chat";
	}
	@PostMapping("/unachieve")
	public String unachieveMsg(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("from") long fromid) {
		Principal userPrincipal = request.getUserPrincipal();
		Student student=service.findStudent(fromid);
       Student mystd=stdrepol.findByEmail(userPrincipal.getName());
			Collection<Messages> msgs=service.updateArchieve(mystd.getId(),student.getId(),1);
			if(msgs!=null) {
				System.out.println("success");
				redirect.addFlashAttribute("messagee", "message un-archieved");
			}
			else {
				System.out.println("error");
				redirect.addFlashAttribute("messagee", "message already un achieved previous time");
				redirect.addFlashAttribute("moja", "1");
			}

     	return "redirect:/chat";
	}

	@PostMapping("/deletemsg")
	public String deleteMsg(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("toid") long msgid,Messages deleteMsg) {
		Student student=service.findStudent(msgid);
		Principal userPrincipal = request.getUserPrincipal();
		System.out.println("deletee");
		Student mystd=stdrepol.findByEmail(userPrincipal.getName());
		Collection<Messages> sendby=service.getSendBy(student.getId(),mystd.getId(),0,0);
		Collection<Messages> sent=service.getSentMsgs(mystd.getId(),student.getId(),0,0);
		for (Messages messages : sendby) {
			deleteMsg=service.deletemsgByid(msgid,mystd.getId(),messages.getId());
			System.out.println("msgid is "+messages.getId());
		}
		for (Messages messages : sent) {
			deleteMsg=service.deletemsgByid(msgid,mystd.getId(),messages.getId());
			System.out.println("msgid is "+messages.getId());
		}
		if(deleteMsg!=null) {
			System.out.println("deleted");
			redirect.addFlashAttribute("messagee", "message deleted");
		}
		return "redirect:/chat";
	}
	@PostMapping("/deletefrommsg")
	public String deleteMsgFrom(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("toid") long msgid,Messages deleteMsg) {
		System.out.println("idii  ni"+msgid);
		Student student=service.findStudent(msgid);

		Principal userPrincipal = request.getUserPrincipal();
		System.out.println("deletee");
		Student mystd=stdrepol.findByEmail(userPrincipal.getName());
		Collection<Messages> sendby=service.getSendBy(mystd.getId(),student.getId(),0,0);
		for (Messages messages : sendby) {
			deleteMsg=service.deletemsgByid(mystd.getId(),msgid,messages.getId());
			System.out.println("msgid is "+messages.getId());
		}
		if(deleteMsg!=null) {
			System.out.println("deleted");
			redirect.addFlashAttribute("messagee", "message deleted");
		}
		return "redirect:/chat";
	}
	@GetMapping("/friends")
	public String viewFrinds(Model m,Authentication auth,HttpServletRequest request,
			RedirectAttributes direct) {
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=service.selectStudent(userPrincipal.getName());
		m.addAttribute("userimage", mystd.getPhotosImagePath());
		m.addAttribute("first", mystd.getFirst());
		m.addAttribute("last", mystd.getLast());
		m.addAttribute("login", mystd.getId());
		m.addAttribute("sdd", mystd);
		Student stud=new Student();
		m.addAttribute("stud", stud);
		Requests reques=new Requests();
		m.addAttribute("reques", reques);
		Collection<Student> listOfStud=service.selectSuggestedRequests(userPrincipal.getName(), 1,0,4,3,mystd.getId());
		m.addAttribute("listOfStudents", listOfStud);
		Collection<Student> requests=service.selectRequests(1,mystd.getId());
		Collection<Student> newFriends=service.selectNewFriends(userPrincipal.getName(), 0,mystd.getId());
		Collection<Student> blocked=service.selectBlockedFriends(userPrincipal.getName(),3,mystd.getId());
		Collection<Student> except=service.selectBlockedFriends(userPrincipal.getName(),0,mystd.getId());
		Collection<Student> whoConfirm=service.whoConfirmRequest(userPrincipal.getName(), 0, mystd.getId());
		Collection<Student> whoConcel=service.whoConfirmRequest(userPrincipal.getName(), 2, mystd.getId());
		m.addAttribute("notisize",(whoConfirm.size()+whoConcel.size()));
		m.addAttribute("whoConfirm",whoConfirm);
		if(except.isEmpty()) {
			m.addAttribute("v1", "1");
			System.out.println("ni 1");
		} 
		else {
			m.addAttribute("v1",except); 
		}

		m.addAttribute("whoConcel",whoConcel);
		m.addAttribute("newFriends",newFriends);
		m.addAttribute("requests", requests);
		m.addAttribute("blocked", blocked);
		return "friends";
	}
	@PostMapping("/sendReq")
	public String sandRequests(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("stid") long id) {
		Principal userPrincipal = request.getUserPrincipal();
		Student student=service.findStudent(id);
		Student  student1=service.saveRequests(userPrincipal.getName(), id,1);
		if(student1!=null) {
			System.out.println("success");
			redirect.addFlashAttribute("message", "Request Sent to "+student.getFirst());
		}
		return "redirect:/friends";
	}
	@PostMapping("/updateReq")
	public String editRequests(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("fromid") long fromid) {
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=service.selectStudent(userPrincipal.getName());
		Student student=service.editRequestStatus(mystd.getId(),fromid,2);
		if(student!=null) {
			System.out.println("updated..");
			redirect.addFlashAttribute("message", "Request from "+student.getFirst()+" Canceled");
		}
		return "redirect:/friends";

	}
	@PostMapping("/Reqconcel")
	public String editRequest(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("conf") long concid) {
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=service.selectStudent(userPrincipal.getName());
		Student student=service.editRequestStatus(mystd.getId(),concid,0);
		if(student!=null) {
			System.out.println("updated..");
			redirect.addFlashAttribute("message", "Now Friends to "+student.getFirst());
		}
		return "redirect:/friends";

	}
	@PostMapping("/blockSt")
	public String blockSt(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("block") long blockid) {
		System.out.println(blockid);
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=service.selectStudent(userPrincipal.getName());
		Student student=service.editRequestStatus(mystd.getId(),blockid,3);
		if(student!=null) {
			System.out.println("blocked..");
			redirect.addFlashAttribute("message",student.getFirst()+" is no longer your friend");
		}
		return "redirect:/friends";

	}
	@PostMapping("/removeSugg")
	public String removeSuggestion(Authentication auth,HttpServletRequest request,RedirectAttributes redirect
			,@RequestParam("remove") long remid) {
		System.out.println(remid);
		Principal userPrincipal = request.getUserPrincipal();
		Student mystd=service.selectStudent(userPrincipal.getName());
		Student student=service.editRequestStatus(mystd.getId(),remid,4);
		if(student!=null) {
			System.out.println("blocked..");
			redirect.addFlashAttribute("message",student.getFirst()+" Doesn't appear any more");
		}
		return "redirect:/friends";

	}
}
