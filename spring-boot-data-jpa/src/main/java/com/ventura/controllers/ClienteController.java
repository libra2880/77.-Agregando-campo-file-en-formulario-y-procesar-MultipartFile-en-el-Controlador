package com.ventura.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ventura.models.entity.Cliente;
import com.ventura.models.service.IClienteService;
import com.ventura.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {


	private IClienteService clienteService;
	
	@Autowired	
	public ClienteController(IClienteService clienteService) {
		
		this.clienteService = clienteService;
	}

	@RequestMapping(value="/listar",method = RequestMethod.GET)
	public String Listar(@RequestParam(name="page",defaultValue = "0") int page, Model model) {
		
		Pageable pageRequest= PageRequest.of(page, 5);
		
		Page<Cliente> clientes=clienteService.findAll(pageRequest);
		
		PageRender<Cliente> pageRender=new PageRender<>("/listar",clientes);
		model.addAttribute("titulo","Listado de Clientes");
		model.addAttribute("clientes",clientes);
		model.addAttribute("page",pageRender);
		
		return "listar";
		//
	}
	
	@RequestMapping(value="/form")
	public String crear(Map<String, Object> model) {
		Cliente cliente=new Cliente();
		model.put("cliente", cliente);
		model.put("titulo","Formulario de Cliente");
		
		return "form";
	}
	@RequestMapping(value="/form/{id}")
	public String editar(@PathVariable(value="id") Long id, Map<String,Object> model,RedirectAttributes flash) {
	
		Cliente cliente=null;
		
		if(id>0) {
			cliente=clienteService.findOne(id);
			  if(cliente==null) {
				  flash.addFlashAttribute("error","El ID del Cliente no existe en la BBDD");
				  return "redirect:/listar";
			  }
		}else {
			flash.addFlashAttribute("error","El ID del Cliente no puede ser cero!");

			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar Cliente");
		
		return "form";
	}
	@RequestMapping(value="/form",method = RequestMethod.POST)
	public String guardar(@Valid  Cliente cliente, BindingResult result,@RequestParam("file") MultipartFile foto,Model model,RedirectAttributes flash,SessionStatus status) {
		
		if(result.hasErrors()) {
			model.addAttribute("titulo","Formulario de Cliente");
			return "form";
		}
		if(!foto.isEmpty()) {
			Path directorioRecursos=Paths.get("src//main//resources//static/uploads");
			String rootPath=directorioRecursos.toFile().getAbsolutePath();
			try {
				byte[] bytes=foto.getBytes();
				Path rutaCompleta=Paths.get(rootPath + "//" + foto.getOriginalFilename());
				Files.write(rutaCompleta, bytes);
				flash.addFlashAttribute("info","Has subido correctamente '" + foto.getOriginalFilename() + "'");
			
				cliente.setFoto(foto.getOriginalFilename());
			
			} catch (IOException e) {
				
				e.printStackTrace();
			}

			
		}
		
		
		String mensajeFlash=(cliente.getId() !=null)? "Cliente editado con éxito!" : "Cliente Creado con éxito!";
		
		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success",mensajeFlash);
		return "redirect:listar";
	}
	
	@RequestMapping(value="/eliminar/{id}")
	public String eliminar(@PathVariable(value="id") Long id ,RedirectAttributes flash) {
		if(id>0) {
			clienteService.delete(id);
			flash.addFlashAttribute("success","Cliente Eliminado con éxito");
			//
		}
		
		return "redirect:/listar";
	}
	
}
