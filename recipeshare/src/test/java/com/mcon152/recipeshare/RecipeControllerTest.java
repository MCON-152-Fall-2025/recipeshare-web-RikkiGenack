package com.mcon152.recipeshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcon152.recipeshare.web.RecipeController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    // Internal class for creation-related tests
    @Nested
    class CreationTests {

        @DisplayName("Adding new recipe")
        @Test
        void testAddRecipe() throws Exception {

            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Cake");
            json.put("description", "Delicious cake");
            // Change ingredients to a single String
            json.put("ingredients", "1 cup of flour, 1 cup of sugar, 3 eggs");
            json.put("instructions", "Mix and bake");
            String jsonString = mapper.writeValueAsString(json);
            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Cake"))
                    .andExpect(jsonPath("$.description").value("Delicious cake"))
                    .andExpect(jsonPath("$.ingredients").value("1 cup of flour, 1 cup of sugar, 3 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @ParameterizedTest
        @CsvSource({
                "'Chocolate Cake','Rich chocolate cake','2 cups flour;1 cup cocoa;4 eggs','Bake at 350F for 30 min'",
                "'Pasta Salad','Fresh pasta salad','200g pasta;100g tomatoes;50g olives','Mix all ingredients'",
                "'Pancakes','Fluffy pancakes','1 cup flour;2 eggs;1 cup milk','Cook on skillet until golden'"
        })
        void parameterizedAddRecipeTest(String title, String description, String ingredients, String instructions) throws Exception {
            throw new UnsupportedOperationException("parameterizedAddRecipeTest");
        }
    }

    // Internal class for delete and get tests
    @Nested
    class DeleteAndGetTests {
        private List<Integer> recipeIds;

        @BeforeEach
        void createRecipes() throws Exception {
            recipeIds = new ArrayList<>();
            String[] recipes = {
                    "{\"title\":\"Pie\",\"description\":\"Apple pie\",\"ingredients\":\"Apples, Flour, Sugar\",\"instructions\":\"Mix and bake\"}",
                    "{\"title\":\"Soup\",\"description\":\"Tomato soup\",\"ingredients\":\"Tomatoes, Water, Salt\",\"instructions\":\"Boil and blend\"}"
            };
            for (String json : recipes) {
                String response = mockMvc.perform(post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                int id = mapper.readTree(response).get("id").asInt();
                recipeIds.add(id);
            }
        }

        @Test
        @DisplayName("Get all recipes")
        void testGetAllRecipes() throws Exception {
            mockMvc.perform(get("/api/recipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Pie"))
                    .andExpect(jsonPath("$[1].title").value("Soup"));
        }

        @Test
        @DisplayName("Get recipe by its ID")
        void testGetRecipe() throws Exception {
            int id = recipeIds.get(0);
            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Pie"));
        }

        @Test
        @DisplayName("Delete recipe test by ID")
        void testDeleteRecipe() {
            int id = recipeIds.get(0);
            mockMvc.perform(delete("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOK())
                    .andExpect(content().string(""));

        }

        @Test
        @DisplayName("Completely update recipe by ID")
        void testPutRecipe() {
            int id = recipeIds.get(0);


            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Updated Cake");
            json.put("description", "Even better cake");
            json.put("ingredients", "Flour, eggs, butter");
            json.put("instructions", "Mix, bake, and cool");

            String jsonString = mapper.writeValueAsString(json);

            // PUT request
            mockMvc.perform(put("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.title").value("Updated Cake"))
                    .andExpect(jsonPath("$.description").value("Even better cake"))
                    .andExpect(jsonPath("$.ingredients").value("Flour, eggs, butter"))
                    .andExpect(jsonPath("$.instructions").value("Mix, bake, and cool"));


            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.title").value("Updated Cake"))
                    .andExpect(jsonPath("$.description").value("Even better cake"))
                    .andExpect(jsonPath("$.ingredients").value("Flour, eggs, butter"))
                    .andExpect(jsonPath("$.instructions").value("Mix, bake, and cool"));
        }

        @Test
        @DisplayName("Partially update recipe by ID")
        void testPatchRecipe() {
            int id = recipeIds.get(0);


            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Updated Cake");
            json.put("description", "Even better cake");
            json.put("instructions", "Mix, bake, and cool");

            String jsonString = mapper.writeValueAsString(json);

            // PUT request
            mockMvc.perform(patch("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.title").value("Updated Cake"))
                    .andExpect(jsonPath("$.description").value("Even better cake"))
                    .andExpect(jsonPath("$.instructions").value("Mix, bake, and cool"));


            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.title").value("Updated Cake"))
                    .andExpect(jsonPath("$.description").value("Even better cake"))
                    .andExpect(jsonPath("$.ingredients").value("2 cups flour;1 cup cocoa;4 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix, bake, and cool"));

        }
    }

    @Nested
    class NonExistingRecipeTests {

        @Test
        @DisplayName("Get Non-existing recipe")
        void testGetNonExistingRecipe() throws Exception {
            // Skeleton: Try to get a recipe with a non-existing ID
            mockMvc.perform(get("/api/recipes/9999"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

        }

        @Test
        @DisplayName("Completely update non-existing recipe")
        void testPutNonExistingRecipe() {
            ObjectNode json = mapper.createObjectNode();
            json.put("name", "pie");
            json.put("description", "delicious");
            json.put("ingredients", "flour, sugar");
            json.put("instructions", "bake");

            String jsonString = mapper.writeValueAsString(json);

            mockMvc.perform(put("/api/recipes/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Partially update non-existing recipe")
        void testPatchNonExistingRecipe()  {

            ObjectNode json = mapper.createObjectNode();
            json.put("name", "pie");

            json.put("ingredients", "flour, sugar");


            String jsonString = mapper.writeValueAsString(json);

            mockMvc.perform(patch("/api/recipes/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Delete non-existing recipe")
        void testDeleteNonExistingRecipe() throws Exception {
            // Skeleton: Try to delete a recipe with a non-existing ID
             mockMvc.perform(delete("/api/recipes/9999"))
                     .andExpect(status().isOk())
                     .andExpect(content().string("false"));

        }
    }



}