package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController extends BaseController {
   @RequestMapping({"/"})
   public String Hello(Model model) {
      model.addAttribute("name", this.hello);
      model.addAttribute("dow", this.dow);
      return "index";
   }

   @RequestMapping({"/reg"})
   public String Reg(Model model, @RequestParam("code") String code) {
      model.addAttribute("name", this.hello);
      model.addAttribute("dow", this.dow);
      model.addAttribute("code", code);
      return "reg";
   }
}
