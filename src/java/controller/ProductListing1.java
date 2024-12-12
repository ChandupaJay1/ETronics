package controller;

import java.nio.file.Files;
import com.google.gson.Gson;
import dto.Response_DTO;
import dto.User_DTO;
import entity.Category;
import entity.Color;
import entity.Model;
import entity.Product;
import entity.Product_Status;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@MultipartConfig
@WebServlet(name = "ProductListing1", urlPatterns = {"/ProductListing1"})
public class ProductListing1 extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Response_DTO response_DTO = new Response_DTO();

        Gson gson = new Gson();

        String categoryId = request.getParameter("categoryId");
        String modelId = request.getParameter("modelId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String colorId = request.getParameter("colorId");
        String price = request.getParameter("price");
        String quantity = request.getParameter("quantity");

        Part image1 = request.getPart("image1");
        Part image2 = request.getPart("image2");
        Part image3 = request.getPart("image3");

        Session session = HibernateUtil.getSessionFactory().openSession();

        if (!Validation.isInteger(categoryId)) {
            response_DTO.setContent("Invalid Category");

        } else if (!Validation.isInteger(modelId)) {
            response_DTO.setContent("Invalid Model");

        } else if (title.isEmpty()) {
            response_DTO.setContent("Please fill Title");

        } else if (description.isEmpty()) {
            response_DTO.setContent("Please fill Description");

        }else if (!Validation.isInteger(colorId)) {

            response_DTO.setContent("Invalid color");

        } else if (price.isEmpty()) {
            response_DTO.setContent("Please fill Price");

        } else if (!Validation.isDouble(price)) {
            response_DTO.setContent("Invalid price");

        } else if (Double.parseDouble(price) <= 0) {
            response_DTO.setContent("Price must be greater than 0");

        } else if (quantity.isEmpty()) {
            response_DTO.setContent("Invalid Quantity");

        } else if (!Validation.isInteger(quantity)) {
            response_DTO.setContent("Invalid Quantity");

        } else if (Integer.parseInt(quantity) <= 0) {
            response_DTO.setContent("Quantity must be greater than 0");

        } else if (image1.getSubmittedFileName() == null) {
            response_DTO.setContent("Please upload image1");

        } else if (image2.getSubmittedFileName() == null) {
            response_DTO.setContent("Please upload image2");

        } else if (image3.getSubmittedFileName() == null) {
            response_DTO.setContent("Please upload image3");

        } else {

            Category category = (Category) session.get(Category.class, Integer.parseInt(categoryId));

            if (category == null) {
                response_DTO.setContent("Please select a valid Category");

            } else {

                Model model = (Model) session.get(Model.class, Integer.parseInt(modelId));

                if (model == null) {
                    response_DTO.setContent("Please select a valid Model");

                } else {

                    if (model.getCategory().getId() != category.getId()) {
                        response_DTO.setContent("Please select a valid Model");

                    }else {

                            Color color = (Color) session.get(Color.class, Integer.parseInt(colorId));

                            if (color == null) {

                                response_DTO.setContent("Please select a valid color");

                            }  else {
                        //to do Insert

                        Product product = new Product();
                        product.setColor(color);
                        product.setDate_time(new Date());
                        product.setDescription(description);
                        product.setModel(model);
                        product.setPrice(Double.parseDouble(price));

                        Product_Status product_Status = (Product_Status) session.load(Product_Status.class, 1);
                        product.setProductStatus(product_Status);
                        product.setQty(Integer.parseInt(quantity));
                        product.setTitle(title);

                        //get user
                        User_DTO userDto = (User_DTO) request.getSession().getAttribute("user");
                        Criteria criteria1 = session.createCriteria(User.class);
                        criteria1.add(Restrictions.eq("email", userDto.getEmail()));
                        User user = (User) criteria1.uniqueResult();
                        product.setUser(user);

                        int pid = (int) session.save(product);
                        session.beginTransaction().commit();

                        String applicationPath = request.getServletContext().getRealPath("");
                        String newApplicationPath = applicationPath.replace("build" + File.separator + "web", "web");

                        File folder = new File(newApplicationPath + "/product-images/" + pid);
                        folder.mkdir();

                        File file1 = new File(folder, "image1.png");
                        InputStream inputStream1 = image1.getInputStream();
                        Files.copy(inputStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println(inputStream1);

                        File file2 = new File(folder, "image2.png");
                        InputStream inputStream2 = image2.getInputStream();
                        Files.copy(inputStream2, file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        

                        File file3 = new File(folder, "image3.png");
                        InputStream inputStream3 = image3.getInputStream();
                        Files.copy(inputStream3, file3.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        response_DTO.setSuccess(true);
                        response_DTO.setContent("New Product Addedd");

                    }

                }

            }

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(response_DTO));
        session.close();

        }
        
    }
}
