package controller;

import com.google.gson.Gson;
import dto.Response_DTO;
import entity.*;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@MultipartConfig
@WebServlet(name = "UpdateProduct", urlPatterns = {"/UpdateProduct"})
public class UpdateProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Response_DTO responseDTO = new Response_DTO();
        Gson gson = new Gson();

        // Get parameters
        String productId = request.getParameter("productId");
        String categoryId = request.getParameter("categoryId");
        String modelId = request.getParameter("modelId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String colorId = request.getParameter("colorId");
        String price = request.getParameter("price");
        String quantity = request.getParameter("quantity");

        // Get image files (optional for update)
        Part image1 = request.getPart("image1");
        Part image2 = request.getPart("image2");
        Part image3 = request.getPart("image3");

        // Start a Hibernate session
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            // Validate inputs
            if (!Validation.isInteger(productId)) {
                responseDTO.setContent("Invalid Product ID");
            } else if (!Validation.isInteger(categoryId)) {
                responseDTO.setContent("Invalid Category");
            } else if (!Validation.isInteger(modelId)) {
                responseDTO.setContent("Invalid Model");
            } else if (title.isEmpty()) {
                responseDTO.setContent("Please fill Title");
            } else if (description.isEmpty()) {
                responseDTO.setContent("Please fill Description");
            } else if (!Validation.isInteger(colorId)) {
                responseDTO.setContent("Invalid Color");
            } else if (price.isEmpty() || !Validation.isDouble(price) || Double.parseDouble(price) <= 0) {
                responseDTO.setContent("Invalid Price");
            } else if (quantity.isEmpty() || !Validation.isInteger(quantity) || Integer.parseInt(quantity) <= 0) {
                responseDTO.setContent("Invalid Quantity");
            } else {
                // Fetch product from database
                Product product = (Product) session.get(Product.class, Integer.parseInt(productId));
                if (product == null) {
                    responseDTO.setContent("Product not found");
                } else {
                    // Fetch related entities
                    Category category = (Category) session.get(Category.class, Integer.parseInt(categoryId));
                    Model model = (Model) session.get(Model.class, Integer.parseInt(modelId));
                    Color color = (Color) session.get(Color.class, Integer.parseInt(colorId));

                    if (category == null) {
                        responseDTO.setContent("Invalid Category");
                    } else if (model == null || model.getCategory().getId() != category.getId()) {
                        responseDTO.setContent("Invalid Model for the selected Category");
                    } else if (color == null) {
                        responseDTO.setContent("Invalid Color");
                    } else {
                        // Update product details
                        session.beginTransaction();
                        product.setCategory(category);
                        product.setModel(model);
                        product.setColor(color);
                        product.setTitle(title);
                        product.setDescription(description);
                        product.setPrice(Double.parseDouble(price));
                        product.setQty(Integer.parseInt(quantity));
                        session.update(product);
                        session.getTransaction().commit();

                        // Update product images (if provided)
                        updateProductImages(request, image1, image2, image3, product.getId());

                        responseDTO.setSuccess(true);
                        responseDTO.setContent("Product updated successfully");
                    }
                }
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            responseDTO.setContent("Error: " + e.getMessage());
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseDTO));
    }

    private void updateProductImages(HttpServletRequest request, Part image1, Part image2, Part image3, int productId) throws IOException {
        String applicationPath = request.getServletContext().getRealPath("");
        String imagesPath = applicationPath.replace("build" + File.separator + "web", "web") + "/product-images/" + productId;

        File imagesFolder = new File(imagesPath);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdir();
        }

        // Update image1 (if provided)
        if (image1 != null && image1.getSubmittedFileName() != null) {
            File file1 = new File(imagesFolder, "image1.png");
            try (InputStream inputStream1 = image1.getInputStream()) {
                Files.copy(inputStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Update image2 (if provided)
        if (image2 != null && image2.getSubmittedFileName() != null) {
            File file2 = new File(imagesFolder, "image2.png");
            try (InputStream inputStream2 = image2.getInputStream()) {
                Files.copy(inputStream2, file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Update image3 (if provided)
        if (image3 != null && image3.getSubmittedFileName() != null) {
            File file3 = new File(imagesFolder, "image3.png");
            try (InputStream inputStream3 = image3.getInputStream()) {
                Files.copy(inputStream3, file3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
