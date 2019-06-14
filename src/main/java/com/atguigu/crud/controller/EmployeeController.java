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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
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
     * 员工更新
     *
     *如果直接发送ajax=PUT请求
     * 封装的数据Employee
     * 将要变更的员工数据：Employee{empId=1020, empName='null', gender='null', email='null', department=null, dId=null}
     *
     * 问题：请求体重有数据
     * 但是Employee对象封装不上
     * 执行的更新数据库的语句就变成了update tbl_emp  where emp_id =1020
     *
     * 原因：
     * Tomcat：
     *      1 将请求体中的数据，封装一个map
     *      2 request.getParameter("empName") 就会从这个map取值
     *      3 SpringMVC封装POJO对象的时候会把POJO中的每个属性值调用request.getParameter("empName")获得；
     *AJAX发送PUT请求引发的血案
     *      PUT请求，请求体中的数据，request.getParameter("empName")拿不到
     *      Tomcat一看是PUT请求不会封装请求体中的数据为map，只有POST形式的请求体为map
     * org.apache.catalina.connector.Request--parseParameters()
     * protected String parseBodyMethods = "POST";
     * if(!getConnector().isParseBpdyMethod(getMethod())){
     *     success= true;
     *     return;
     * }
     *结局方案：
     * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
     * 配置上HttpPutFormContentFilter
     * 他的作用，将请求体中的数据解析包装成一个map。之后request被重新包装，request.getParameter()就被重写，就从自己封装的map中取数据
     * @param employee
     * @return
     */
    @RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee, HttpServletRequest request){
        System.out.println("请求体中的值："+request.getParameter("gender"));
        System.out.println("将要变更的员工数据："+employee);
        employeeService.updateEmp(employee);
        return Msg.success();
    }

    /**
     * 批量删除：1-2-3
     * 单个删除：1
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/emp/{ids}")
    public Msg deleteEmpById(@PathVariable("ids")String ids){
        if(ids.contains("-")){//批量删除
            String[] str_ids = ids.split("-");
            List<Integer> del_ids = new ArrayList<>();

            for(String string : str_ids){
                del_ids.add(Integer.parseInt(string));
            }
            employeeService.deleteBatch(del_ids);
        }else {//单个删除
            Integer id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){
        Employee employee = employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
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
        //String orderBy = "emp_id desc";
        PageHelper.startPage(pn,5);
        PageHelper.orderBy("emp_id ASC");
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
