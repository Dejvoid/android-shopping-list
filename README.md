# Shopping list app

The application allows user to locally manage his home storage and shopping list.

## Screens

1. (Default) Storage List
   - User can track their products (name, count and expiry)
   - User can see which products are expired, about to expire or run out
3. Shopping List
   - User can export to Storage List (when user completes their shopping, he can select which products he bought and put them in their storage list in one click)
   - User can add products with counts and expiry dates
3. Product Detail
   - Serves as an editor for new or already existing products. Allows information update or deletion.
  
## Layout

- Right-side slide in menu - navigation between the screens
- Bottom Bar - In list screens, contains `+` button to add new products
- Product card - expandable information about the product
    - In Shopping List screen can be ticked to select multiple products for the export to Storage list
    - Collapsible on click
    - Edit button - opens Product Detail screen
 
## Architecture and technologies

The application uses Jetpack Compose and Room.

From the architecture point of view, the application follows MVVM pattern. The UI works with ViewModel classes which then update the Model via the Room database access.

The database could be easily exchanged for another technology (e.g. network API) by simply implementing `ProductListApi` interface.

The access to the database follows the recommended pattern for Room - accessing the model via DAO class (`ProductDataDao`).
