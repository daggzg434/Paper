package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController extends BaseController {
   @RequestMapping({"/admin"})
   public String Hello(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/sign-in";
   }
}
