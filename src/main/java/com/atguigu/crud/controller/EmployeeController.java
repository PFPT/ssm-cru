package com.atguigu.crud.controller;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工CRUD请求
 */
@Controller
public class EmployeeController {
    /**
     * 查询员工数据（分页查询）
     * @return
     */
    @Autowired
    EmployeeService employeeService;

    /**
     * 检查员工姓名是否存在
     */
    @RequestMapping("/checkName")
    @ResponseBody
    public Msg checkuser(@RequestParam(value = "empName") String empName){
        //先判断用户名是否是合法的表达式
        String regName = "(^[a-zA-Z0-9_-]{2,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if(!empName.matches(regName)){
            return  Msg.fail().add("va_msg","用户名可以是2-16位英文和数字或者2-5位中文");
        }
        //数据库用户名重复校验
        boolean resault = employeeService.checkUser(empName);
        if(resault)
            return Msg.success();
        return Msg.fail().add("va_msg","用户名不可用");
    }

    /**
     *员工保存，注解POST请求，表示保存。put表示修改，get表示查询
     * 1.支持JSR303校验
     * 2.导入Hibernate-Validator（这是对JSR303的实现）
     *Valid注解 表示运行Employee的成员变量声明上边的校验注解，BindingResult，存放校验结果
     * @return
     */
    @RequestMapping(value = "/emp",method= RequestMethod.POST)
    @ResponseBody
    public  Msg saveEmp(@Valid Employee employee, BindingResult result){
        if (result.hasErrors())
        {
            //校验失败，应该返回失败，在模态框中显示校验失败的错误信息
            Map<String,Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for(FieldError fieldError:errors){
                System.out.println("错误的字段名："+fieldError.getField());
                System.out.println("错误信息："+fieldError.getDefaultMessage());
                map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        }
        //校验成功
        employeeService.saveEmp(employee);
        return Msg.success();
    }

    /**
     * 要使得注解ResponseBody正常工作，需要导入jackson包
     * 这样就成功把分页查询改造成了ajax方法
     * @param pn
     * @return
     */
    @RequestMapping("/emps")
    @ResponseBody//把pageInfo转为Json数据
    public Msg getEmpsWithJson(@RequestParam(value = "pn",defaultValue = "1")Integer pn){
        //这不是一个分页查询
        //引入PageHelper 分页插件
        //在查询之前只需要调用,传入页码，以及每页的大小
        PageHelper.startPage(pn,5);
        //startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps=employeeService.getAll();
        //使用pageInfo保证查询和的结果，只需要将pageInfo交给页面就行
        PageInfo page=new PageInfo(emps,5);
        return  Msg.success().add("pageInfo",page);
    }
//处理的就是emps的请求
 //   @RequestMapping("/emps")
        public String getEmps(@RequestParam(value = "pn",defaultValue = "1")Integer pn, Model model){
            //这不是一个分页查询
            //引入PageHelper 分页插件
            //在查询之前只需要调用,传入页码，以及每页的大小
            PageHelper.startPage(pn,5);
            //startPage后面紧跟的这个查询就是一个分页查询
            List<Employee> emps=employeeService.getAll();
            //使用pageInfo保证查询和的结果，只需要将pageInfo交给页面就行
            //model请求域封装了详细的分页信息，包括有我们查询出来的数据，传入连续显示的页数（5）
            PageInfo page=new PageInfo(emps,5);
            model.addAttribute("pageInfo",page);

            return  "list";
        }
}
