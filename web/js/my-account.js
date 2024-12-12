var modelList;
var productList;

async function loadFeatures() {
    try {
        const response = await fetch("LoadFeatures");
        if (response.ok) {
            const json = await response.json();

            const categoryList = json.categoryList || [];
            modelList = json.modelList || [];
            productList = json.productList || [];
            const colorList = json.colorList || [];

            // Safely load dropdowns
            if (categoryList.length) loadSelect("category-select", categoryList, "name");
            if (modelList.length) loadSelect("model-select", modelList, "name");
            if (colorList.length) loadSelect("colorSelect", colorList, "name");
            if (productList.length) loadSelect("product-select", productList, "title");
        } else {
            console.error("Error loading features: ", response.statusText);
        }
    } catch (error) {
        console.error("Error in loadFeatures:", error);
    }
}

function loadSelect(selectTagId, list, property) {
    const selectTag = document.getElementById(selectTagId);
    if (!selectTag) {
        console.error(`Element with ID ${selectTagId} not found.`);
        return;
    }

    // Clear previous options (if any)
    selectTag.innerHTML = `<option value="">--Select--</option>`;

    // Add new options
    list.forEach(item => {
        let optionTag = document.createElement("option");
        optionTag.value = item.id;
        optionTag.innerHTML = item[property];
        selectTag.appendChild(optionTag);
    });
}

function updateModels() {
    const modelSelectTag = document.getElementById("model-select");
    if (!modelSelectTag) return;

    modelSelectTag.innerHTML = `<option value="">--Select Model--</option>`;
    const selectedCategoryId = document.getElementById("category-select")?.value;

    modelList.forEach(model => {
        if (model.category.id == selectedCategoryId) {
            let optionTag = document.createElement("option");
            optionTag.value = model.id;
            optionTag.innerHTML = model.name;
            modelSelectTag.appendChild(optionTag);
        }
    });
}


function fillProductDetails(productId) {
    const product = productList?.find(p => p.id == productId);
    if (product) {
        document.getElementById("category-select").value = product.category.id;
        updateModels();
        document.getElementById("model-select").value = product.model.id;
        document.getElementById("title").value = product.title;
        document.getElementById("description").value = product.description;
        document.getElementById("colorSelect").value = product.color.id;
        document.getElementById("price").value = product.price;
        document.getElementById("quantity").value = product.quantity;
    } else {
        console.warn(`Product with ID ${productId} not found.`);
    }


    async function productListining() {
        const categorySelect = document.getElementById("category-select");
        const modelSelect = document.getElementById("model-select");
        const title = document.getElementById("title");
        const description = document.getElementById("description");
        const colorTag = document.getElementById("colorSelect");
        const price = document.getElementById("price");
        const quantity = document.getElementById("quantity");
        const image1 = document.getElementById("image1");
        const image2 = document.getElementById("image2");
        const image3 = document.getElementById("image3");
    
        if (!categorySelect || !modelSelect || !title || !description || !colorTag || !price || !quantity) {
            console.error("One or more required elements are missing in the DOM.");
            return;
        }
    
        const data = new FormData();
        data.append("categoryId", categorySelect.value);
        data.append("modelId", modelSelect.value);
        data.append("title", title.value);
        data.append("description", description.value);
        data.append("colorId", colorTag.value);
        data.append("price", price.value);
        data.append("quantity", quantity.value);
        if (image1?.files[0]) data.append("image1", image1.files[0]);
        if (image2?.files[0]) data.append("image2", image2.files[0]);
        if (image3?.files[0]) data.append("image3", image3.files[0]);
    
        try {
            const response = await fetch("ProductListing1", {
                method: "POST",
                body: data,
            });
    
            if (response.ok) {
                const json = await response.json();
                const popup = new Notification();
    
                if (json.success) {
                    // Reset form
                    categorySelect.value = 0;
                    modelSelect.value = 0;
                    title.value = "";
                    description.value = "";
                    colorTag.value = 0;
                    price.value = "";
                    quantity.value = 1;
                    if (image1) image1.value = null;
                    if (image2) image2.value = null;
                    if (image3) image3.value = null;
    
                    popup.success({
                        message: json.content,
                    });
                } else {
                    popup.error({
                        message: json.content,
                    });
                }
            } else {
                console.error("Failed to list product:", response.statusText);
            }
        } catch (error) {
            console.error("Error listing product:", error);
        }
    }

// Function to update a product
}