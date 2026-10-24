package com.ecommerce.store;

import com.ecommerce.store.product.*;
import com.ecommerce.store.user.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {
    @Bean CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            List<Product> sampleProducts = List.of(
                    new Product("Everyday Ceramic Mug", "A durable stoneware cup for your morning chai or filter coffee.", "Home", new BigDecimal("499.00"), 30, "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?auto=format&fit=crop&w=900&q=85"),
                    new Product("Canvas Weekender", "A roomy, hard-wearing carry bag for weekend escapes and daily travel.", "Accessories", new BigDecimal("1999.00"), 14, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=85"),
                    new Product("Desk Lamp", "Warm, adjustable light for late-night reading and focused work.", "Home", new BigDecimal("1299.00"), 20, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=85"),
                    new Product("Botanical Notebook", "A lay-flat recycled-paper notebook for notes, lists, and new ideas.", "Stationery", new BigDecimal("249.00"), 45, "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=900&q=85"),
                    new Product("Linen Throw", "A soft, breathable layer for relaxed evenings at home.", "Home", new BigDecimal("1999.00"), 9, "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=900&q=85"),
                    new Product("Stoneware Serving Bowl", "A hand-finished bowl for family meals, snacks, and festive tables.", "Home", new BigDecimal("699.00"), 18, "https://images.unsplash.com/photo-1610701596007-11502861dcfa?auto=format&fit=crop&w=900&q=85"),
                    new Product("Amber Glass Vase", "Sculptural tinted glass to bring a warm accent to your home.", "Home", new BigDecimal("699.00"), 12, "https://images.unsplash.com/photo-1578500494198-246f612d3b3d?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Market Tote", "A reusable cotton carry-all for the local market and everyday errands.", "Accessories", new BigDecimal("399.00"), 25, "https://images.unsplash.com/photo-1590874103328-eac38a683ce7?auto=format&fit=crop&w=900&q=85"),
                    new Product("Leather Card Holder", "A slim wallet for cards and the everyday essentials.", "Accessories", new BigDecimal("699.00"), 16, "https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=900&q=85"),
                    new Product("Desk Organizer", "A simple organizer to keep your pens, notes, and work table tidy.", "Stationery", new BigDecimal("799.00"), 14, "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=900&q=85"),
                    new Product("Hardcover Journal", "A thoughtfully bound journal for ideas, plans, and daily reflections.", "Stationery", new BigDecimal("349.00"), 32, "https://images.unsplash.com/photo-1531346878377-a5be20888e57?auto=format&fit=crop&w=900&q=85"),
                    new Product("Scented Candle", "A clean-burning candle with a soft sandalwood and citrus fragrance.", "Home", new BigDecimal("499.00"), 21, "https://images.unsplash.com/photo-1603006905003-be475563bc59?auto=format&fit=crop&w=900&q=85"),
                    new Product("Woven Cushion Cover", "A textured cotton cushion cover in a relaxed natural weave.", "Home", new BigDecimal("699.00"), 11, "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=900&q=85"),
                    new Product("Ceramic Planter", "A minimal planter to bring a little more green into your home.", "Home", new BigDecimal("499.00"), 19, "https://images.unsplash.com/photo-1485955900006-10f4d324d411?auto=format&fit=crop&w=900&q=85"),
                    new Product("Insulated Travel Bottle", "A reusable insulated bottle for your commute, campus, or travels.", "Accessories", new BigDecimal("699.00"), 27, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=85"),
                    new Product("Brass Diya Set", "A pair of polished brass diyas for a warm glow during evening prayers and celebrations.", "Home", new BigDecimal("599.00"), 18, "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Table Runner", "A woven cotton runner that brings an easy, colourful accent to everyday meals.", "Home", new BigDecimal("899.00"), 12, "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=900&q=85"),
                    new Product("Jute Storage Basket", "A sturdy handwoven basket for keeping throws, toys, and household essentials in order.", "Home", new BigDecimal("799.00"), 15, "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=85"),
                    new Product("Masala Dabba", "A stainless-steel spice box with seven cups for keeping everyday cooking essentials close at hand.", "Home", new BigDecimal("749.00"), 22, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=85"),
                    new Product("Acacia Serving Tray", "A smooth-finished wooden tray for chai, snacks, and relaxed weekend breakfasts.", "Home", new BigDecimal("1099.00"), 10, "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=900&q=85"),
                    new Product("Block Print Cushion Cover", "A cotton cushion cover with a handcrafted-inspired print for a fresh living-room update.", "Home", new BigDecimal("549.00"), 20, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85"),
                    new Product("Terracotta Water Bottle", "A naturally cool-feeling clay bottle for serving water at home. Handle with care.", "Home", new BigDecimal("399.00"), 16, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=85"),
                    new Product("Brass Incense Holder", "A compact brass holder to keep your favourite incense neatly in place.", "Home", new BigDecimal("349.00"), 24, "https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Zip Pouch", "A lightweight zip pouch for chargers, stationery, and small everyday essentials.", "Accessories", new BigDecimal("299.00"), 35, "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=85"),
                    new Product("Everyday Sling Bag", "A compact crossbody bag with room for your phone, wallet, and daily commute essentials.", "Accessories", new BigDecimal("1199.00"), 13, "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=900&q=85"),
                    new Product("Minimalist Key Holder", "A slim leather key case that keeps your everyday keys together while you are on the move.", "Accessories", new BigDecimal("449.00"), 28, "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=900&q=85"),
                    new Product("Weekly Planner Pad", "An undated desk planner for organising work, errands, and the week ahead.", "Stationery", new BigDecimal("199.00"), 40, "https://images.unsplash.com/photo-1598532163257-ae3c6b2524b6?auto=format&fit=crop&w=900&q=85"),
                    new Product("Brass Bookmark", "A slim metal bookmark with a warm brass finish for your current read.", "Stationery", new BigDecimal("179.00"), 30, "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=900&q=85"),
                    new Product("Watercolour Art Set", "A beginner-friendly set for sketching and painting during a quiet afternoon.", "Stationery", new BigDecimal("649.00"), 17, "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=900&q=85"),
                    new Product("Handmade Paper Gift Tags", "A pack of textured paper tags for adding a thoughtful finish to gifts and festive hampers.", "Stationery", new BigDecimal("149.00"), 50, "https://images.unsplash.com/photo-1485230895905-ec40ba36b9bc?auto=format&fit=crop&w=900&q=85"),
                    new Product("Indigo Cotton Table Napkins", "A set of four soft cotton napkins in a deep indigo shade for everyday dining.", "Home", new BigDecimal("449.00"), 24, "https://images.unsplash.com/photo-1503341504253-dff4815485f1?auto=format&fit=crop&w=900&q=85"),
                    new Product("Handwoven Seagrass Basket", "A natural woven basket for organising linens, magazines, or children's toys.", "Home", new BigDecimal("999.00"), 11, "https://images.unsplash.com/photo-1515377905703-c4788e51af15?auto=format&fit=crop&w=900&q=85"),
                    new Product("Ceramic Chai Kulhad Set", "A set of four reusable ceramic cups inspired by the familiar warmth of chai kulhads.", "Home", new BigDecimal("599.00"), 20, "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=900&q=85"),
                    new Product("Mango Wood Coaster Set", "Six smooth-finished wooden coasters to protect your table during chai breaks.", "Home", new BigDecimal("349.00"), 30, "https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Dohar", "A lightweight, breathable cotton layer for comfortable nights through warmer seasons.", "Home", new BigDecimal("1799.00"), 8, "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=85"),
                    new Product("Rattan Desk Tray", "A compact woven tray for keys, stationery, and small everyday essentials.", "Home", new BigDecimal("549.00"), 16, "https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=900&q=85"),
                    new Product("Ceramic Oil Dispenser", "A glazed ceramic dispenser with a pour spout for keeping cooking oil close at hand.", "Home", new BigDecimal("649.00"), 14, "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=900&q=85"),
                    new Product("Festive Tealight Holder Set", "Three decorative holders for creating a gentle glow at home during celebrations.", "Home", new BigDecimal("499.00"), 19, "https://images.unsplash.com/photo-1523413651479-597eb2da0ad6?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Door Mat", "A durable cotton-blend mat with a simple woven texture for the entryway.", "Home", new BigDecimal("399.00"), 22, "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=900&q=85"),
                    new Product("Stoneware Breakfast Plate Set", "A set of two everyday stoneware plates for breakfast, snacks, and light meals.", "Home", new BigDecimal("899.00"), 12, "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Block Print Cushion", "A comfortable cotton cushion with a block-print-inspired cover for a relaxed sofa.", "Home", new BigDecimal("799.00"), 13, "https://images.unsplash.com/photo-1540932239986-30128078f3c5?auto=format&fit=crop&w=900&q=85"),
                    new Product("Bamboo Serving Spoons", "A pair of lightweight bamboo serving spoons for salads, rice, and shared meals.", "Home", new BigDecimal("299.00"), 27, "https://images.unsplash.com/photo-1490312278390-ab64016e0aa9?auto=format&fit=crop&w=900&q=85"),
                    new Product("Handpainted Ceramic Planter", "A small ceramic planter with a colourful finish for a favourite indoor plant.", "Home", new BigDecimal("599.00"), 15, "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Apron", "A practical cotton kitchen apron with front pockets for everyday cooking.", "Home", new BigDecimal("499.00"), 18, "https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=900&q=85"),
                    new Product("Brass Pooja Thali", "A compact brass-finish prayer plate for keeping daily ritual essentials together.", "Home", new BigDecimal("899.00"), 9, "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=900&q=85"),
                    new Product("Canvas Laptop Sleeve", "A padded canvas sleeve to help protect a laptop during commutes and campus days.", "Accessories", new BigDecimal("999.00"), 14, "https://images.unsplash.com/photo-1616486029423-aaa4789e8c9a?auto=format&fit=crop&w=900&q=85"),
                    new Product("Everyday Cotton Scarf", "A soft, versatile cotton scarf for a comfortable layer through changing weather.", "Accessories", new BigDecimal("599.00"), 21, "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=85"),
                    new Product("Foldable Shopping Bag", "A reusable foldable bag that tucks away easily for market trips and errands.", "Accessories", new BigDecimal("249.00"), 40, "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=900&q=85"),
                    new Product("Travel Toiletry Pouch", "A zippered pouch with room for travel-size toiletries and grooming essentials.", "Accessories", new BigDecimal("499.00"), 20, "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=900&q=85"),
                    new Product("Leather Passport Cover", "A compact passport sleeve to keep travel documents together on your next trip.", "Accessories", new BigDecimal("799.00"), 12, "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=900&q=85"),
                    new Product("Insulated Lunch Bag", "A reusable carry bag for bringing lunch to work, school, or a day out.", "Accessories", new BigDecimal("649.00"), 17, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=900&q=85"),
                    new Product("Cotton Hair Scrunchie Set", "A set of soft fabric hair ties for a comfortable everyday hold.", "Accessories", new BigDecimal("199.00"), 45, "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=900&q=85"),
                    new Product("Minimal Crossbody Wallet", "A slim zip wallet with a detachable strap for carrying the essentials hands-free.", "Accessories", new BigDecimal("899.00"), 10, "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=900&q=85"),
                    new Product("Recycled Paper Sketchbook", "A spiral-bound sketchbook with recycled pages for drawing and daily ideas.", "Stationery", new BigDecimal("299.00"), 32, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=85"),
                    new Product("Fine Line Pen Set", "A set of three smooth-writing pens for notes, journaling, and everyday planning.", "Stationery", new BigDecimal("249.00"), 38, "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=900&q=85"),
                    new Product("Daily Habit Tracker", "A simple undated tracker pad for building routines one day at a time.", "Stationery", new BigDecimal("179.00"), 35, "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=85"),
                    new Product("Watercolour Postcard Set", "A set of blank art postcards for painting, thoughtful notes, and small gifts.", "Stationery", new BigDecimal("299.00"), 26, "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=900&q=85"),
                    new Product("Desk Calendar", "An undated tabletop calendar to keep plans and important reminders in view.", "Stationery", new BigDecimal("349.00"), 23, "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85"),
                    new Product("Kraft Gift Wrap Set", "A set of recyclable kraft paper sheets and twine for wrapping thoughtful gifts.", "Stationery", new BigDecimal("249.00"), 31, "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=900&q=85"),
                    new Product("Pocket Address Book", "A compact alphabetical address book for keeping important contacts close by.", "Stationery", new BigDecimal("229.00"), 28, "https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=900&q=85")
            );
            Map<String, List<BigDecimal>> previousSeedPrices = Map.ofEntries(
                    Map.entry("Everyday Ceramic Mug", List.of(new BigDecimal("18.00"), new BigDecimal("599.00"))),
                    Map.entry("Canvas Weekender", List.of(new BigDecimal("64.00"), new BigDecimal("2499.00"))),
                    Map.entry("Desk Lamp", List.of(new BigDecimal("42.50"), new BigDecimal("1799.00"))),
                    Map.entry("Botanical Notebook", List.of(new BigDecimal("12.00"), new BigDecimal("399.00"))),
                    Map.entry("Linen Throw", List.of(new BigDecimal("78.00"), new BigDecimal("2999.00"))),
                    Map.entry("Stoneware Serving Bowl", List.of(new BigDecimal("1299.00"))),
                    Map.entry("Amber Glass Vase", List.of(new BigDecimal("999.00"))),
                    Map.entry("Cotton Market Tote", List.of(new BigDecimal("699.00"))),
                    Map.entry("Leather Card Holder", List.of(new BigDecimal("899.00"))),
                    Map.entry("Desk Organizer", List.of(new BigDecimal("1199.00"))),
                    Map.entry("Hardcover Journal", List.of(new BigDecimal("499.00"))),
                    Map.entry("Scented Candle", List.of(new BigDecimal("799.00"))),
                    Map.entry("Woven Cushion Cover", List.of(new BigDecimal("1099.00"))),
                    Map.entry("Ceramic Planter", List.of(new BigDecimal("899.00"))),
                    Map.entry("Insulated Travel Bottle", List.of(new BigDecimal("999.00")))
            );
            Set<String> previouslyReusedPhotoIds = Set.of(
                    "1544787219-7f47ccb76574", "1556911220-bff31c812dba", "1584100936595-c0654b55a2e2",
                    "1544816155-12df9643f363", "1531346878377-a5be20888e57", "1603006905003-be475563bc59",
                    "1553062407-98eeb64c6a62", "1578749556568-bc2c40e68b61", "1485955900006-10f4d324d411",
                    "1600210492486-724fe5c67fb0", "1529139574466-a303027c1d8b", "1590874103328-eac38a683ce7",
                    "1498050108023-c5249f4df085", "1513519245088-0e12902e5a38", "1595428774223-ef52624120d2",
                    "1602143407151-7111542de6e8", "1627123424574-724758594e93", "1610701596007-11502861dcfa",
                    "1513364776144-60967b0f800f"
            );
            for (int index = 0; index < sampleProducts.size(); index++) {
                Product product = sampleProducts.get(index);
                var existing = products.findByNameIgnoreCase(product.getName());
                if (existing.isEmpty()) {
                    products.save(product);
                    continue;
                }
                Product current = existing.get();
                boolean changed = false;
                List<BigDecimal> previousPrices = previousSeedPrices.getOrDefault(product.getName(), List.of());
                if (previousPrices.stream().anyMatch(price -> current.getPrice().compareTo(price) == 0)) {
                    current.update(product.getName(), product.getDescription(), product.getCategory(),
                            product.getPrice(), current.getStock(), product.getImageUrl());
                    changed = true;
                }
                if (index >= 15 && current.getImageUrl() != null
                        && previouslyReusedPhotoIds.stream().anyMatch(photoId ->
                        current.getImageUrl().contains("/photo-" + photoId + "?"))) {
                    current.setImageUrl(product.getImageUrl());
                    changed = true;
                }
                if (changed) {
                    products.save(current);
                }
            }
        };
    }
    @Bean CommandLineRunner bootstrapAdmin(UserRepository users, PasswordEncoder encoder,
                                           @org.springframework.beans.factory.annotation.Value("${ADMIN_EMAIL:}") String email,
                                           @org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.isBlank()) throw new IllegalStateException("Set both ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap an admin");
            if (password.length() < 14) throw new IllegalStateException("ADMIN_PASSWORD must be at least 14 characters");
            String normalized = email.trim().toLowerCase();
            users.findByEmailIgnoreCase(normalized).ifPresentOrElse(existing -> {
                if (existing.getRole() != Role.ADMIN) throw new IllegalStateException("ADMIN_EMAIL belongs to a non-admin account");
            }, () -> users.save(new AppUser("Store Admin", normalized, encoder.encode(password), Role.ADMIN)));
        };
    }
}
