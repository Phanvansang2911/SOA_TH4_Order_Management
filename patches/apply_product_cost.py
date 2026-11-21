from pathlib import Path

base = (Path(__file__).resolve().parents[1] / "product-service"
        / "src" / "main" / "java" / "com" / "example" / "product_service")
model = base / "model" / "Product.java"
controller = base / "controller" / "ProductController.java"

if not model.exists() or not controller.exists():
    raise SystemExit("Khong tim thay Product.java hoac ProductController.java. Hay dat patches o thu muc goc SOA_TH4.")

s = model.read_text(encoding="utf-8")
if "costPrice" not in s:
    target = "    @Id"
    if target not in s:
        raise SystemExit("Khong tim thay @Id trong Product.java; chua thay doi file.")
    insert = """    // Gia nhap phuc vu tinh chi phi va loi nhuan.
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

"""
    s = s.replace(target, insert + target, 1)
    model.write_text(s, encoding="utf-8")

c = controller.read_text(encoding="utf-8")
if "old.setCostPrice" not in c:
    target = "old.setPrice(p.getPrice());"
    if target not in c:
        raise SystemExit("Khong tim thay setPrice trong ProductController.java; can cap nhat thu cong.")
    c = c.replace(target, target + "\n            old.setCostPrice(p.getCostPrice());", 1)
    controller.write_text(c, encoding="utf-8")

print("Thanh cong: da bo sung costPrice vao Product va ProductController.")
