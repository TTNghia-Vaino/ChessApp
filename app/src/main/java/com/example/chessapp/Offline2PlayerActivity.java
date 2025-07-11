package com.example.chessapp;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import android.widget.SimpleAdapter;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


public class Offline2PlayerActivity extends AppCompatActivity {

    GridLayout chessBoard;
    private ImageView[][] cacOVuong = new ImageView[8][8];
    private QuanCo[][] banCo = new QuanCo[8][8];
    private boolean luotTrang = true;
    private ImageView oVuongDaChon = null;
    private int hangDaChon = -1, cotDaChon = -1;
    private List<int[]> cacNuocDiHopLe = new ArrayList<>();
    private boolean gameKetThuc = false;
    private boolean dangBiChieu = false;
    private int mauOSang = Color.parseColor("#EEEED2");
    private int mauOToi = Color.parseColor("#769656");
    // Nhập thành bên trắng
    private boolean daDiChuyenVuaTrang = false;
    private boolean[] daDiChuyenXeTrang = {false, false}; // [0]: a1, [1]: h1

    // Nhập thành bên đen
    private boolean daDiChuyenVuaDen = false;
    private boolean[] daDiChuyenXeDen = {false, false};   // [0]: a8, [1]: h8

    enum LoaiQuan {
        TOT, XE, MA, TUONG, HAU, VUA
    }
    class QuanCo {
        LoaiQuan loai;
        boolean laTrang;
        boolean daDiChuyen = false;

        QuanCo(LoaiQuan loai, boolean laTrang) {
            this.loai = loai;
            this.laTrang = laTrang;
        }

        public String layTenFile() {
            String prefix = laTrang ? "w" : "b";
            switch (loai) {
                case VUA: return prefix + "k";      // wk.png hoặc bk.png
                case HAU: return prefix + "q";      // wq.png hoặc bq.png
                case XE: return prefix + "r";       // wr.png hoặc br.png
                case TUONG: return prefix + "b";    // wb.png hoặc bb.png
                case MA: return prefix + "n";       // wn.png hoặc bn.png
                case TOT: return prefix + "p";      // wp.png hoặc bp.png
            }
            return "";
        }


        public String layKyHieu() {
            if (laTrang) {
                switch (loai) {
                    case VUA: return "♔";
                    case HAU: return "♕";
                    case XE: return "♖";
                    case TUONG: return "♗";
                    case MA: return "♘";
                    case TOT: return "♙";
                }
            } else {
                switch (loai) {
                    case VUA: return "♚";
                    case HAU: return "♛";
                    case XE: return "♜";
                    case TUONG: return "♝";
                    case MA: return "♞";
                    case TOT: return "♟";
                }
            }
            return "";
        }
    }
    public class BuocDi {
        int tuHang, tuCot, denHang, denCot;
        QuanCo quanDiChuyen;
        QuanCo quanBiAn;
        boolean daDiChuyenCu;
        boolean luotTrang; // Lưu lượt người chơi trước khi đi

        public BuocDi(int tuHang, int tuCot, int denHang, int denCot,
                      QuanCo quanDiChuyen, QuanCo quanBiAn, boolean luotTrang) {
            this.tuHang = tuHang;
            this.tuCot = tuCot;
            this.denHang = denHang;
            this.denCot = denCot;
            this.quanDiChuyen = quanDiChuyen;
            this.quanBiAn = quanBiAn;
            this.daDiChuyenCu = quanDiChuyen.daDiChuyen;
            this.luotTrang = luotTrang;
        }
    }

    private Stack<BuocDi> lichSuBuocDi = new Stack<>();

    private LinearLayout thanhKetThucGame;
    private Button btnChoiLai, btnThoat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_board);
        setTitle("Chơi 2 người - Lượt: Trắng");

        Button btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(v -> undoNuocDi());

        thanhKetThucGame = findViewById(R.id.thanhKetThucGame);
        btnChoiLai = findViewById(R.id.btnChoiLai);
        btnThoat = findViewById(R.id.btnThoat);

        btnChoiLai.setOnClickListener(v -> khoiTaoLaiGame());
        btnThoat.setOnClickListener(v -> finish());

        chessBoard = findViewById(R.id.chessBoard);
        veBanCo();
        // đổi bàn cờ từ đủ quân sang ending để test
        datBanCoCustom();
        //datQuanCoBanDau();

        capNhatHienThi();

        kiemTraTrangThaiGame();
    }
    private void veBanCo() {
        int kichThuoc = 8;
        int kichThuocO = getResources().getDisplayMetrics().widthPixels / kichThuoc;

        for (int hang = 0; hang < kichThuoc; hang++) {
            for (int cot = 0; cot < kichThuoc; cot++) {
                ImageView oVuong = new ImageView(this);

                GridLayout.LayoutParams thamSo = new GridLayout.LayoutParams();
                thamSo.width = kichThuocO;
                thamSo.height = kichThuocO;
                thamSo.rowSpec = GridLayout.spec(hang);
                thamSo.columnSpec = GridLayout.spec(cot);
                thamSo.setMargins(1, 1, 1, 1);

                oVuong.setLayoutParams(thamSo);
                oVuong.setBackgroundColor((hang + cot) % 2 == 0 ? mauOSang : mauOToi);
                oVuong.setScaleType(ImageView.ScaleType.CENTER);

                final int hangHienTai = hang;
                final int cotHienTai = cot;
                oVuong.setOnClickListener(v -> xuLyNhanVaoO(hangHienTai, cotHienTai));

                cacOVuong[hang][cot] = oVuong;
                chessBoard.addView(oVuong);
            }
        }
    }
    private void datQuanCoBanDau() {
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                banCo[hang][cot] = null;
            }
        }

        // Đặt quân trắng
        banCo[7][0] = new QuanCo(LoaiQuan.XE, true);
        banCo[7][1] = new QuanCo(LoaiQuan.MA, true);
        banCo[7][2] = new QuanCo(LoaiQuan.TUONG, true);
        banCo[7][3] = new QuanCo(LoaiQuan.HAU, true);
        banCo[7][4] = new QuanCo(LoaiQuan.VUA, true);
        banCo[7][5] = new QuanCo(LoaiQuan.TUONG, true);
        banCo[7][6] = new QuanCo(LoaiQuan.MA, true);
        banCo[7][7] = new QuanCo(LoaiQuan.XE, true);

        // Tốt trắng
        for (int cot = 0; cot < 8; cot++) {
            banCo[6][cot] = new QuanCo(LoaiQuan.TOT, true);
        }

        // Đặt quân đen (hàng trên - hàng 0 và 1)
        banCo[0][0] = new QuanCo(LoaiQuan.XE, false);
        banCo[0][1] = new QuanCo(LoaiQuan.MA, false);
        banCo[0][2] = new QuanCo(LoaiQuan.TUONG, false);
        banCo[0][3] = new QuanCo(LoaiQuan.HAU, false);
        banCo[0][4] = new QuanCo(LoaiQuan.VUA, false);
        banCo[0][5] = new QuanCo(LoaiQuan.TUONG, false);
        banCo[0][6] = new QuanCo(LoaiQuan.MA, false);
        banCo[0][7] = new QuanCo(LoaiQuan.XE, false);

        // Tốt đen
        for (int cot = 0; cot < 8; cot++) {
            banCo[1][cot] = new QuanCo(LoaiQuan.TOT, false);
        }
    }
    private void datBanCoCustom() {
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                banCo[hang][cot] = null;
            }
        }
        daDiChuyenVuaTrang = true;
        // Reset mảng boolean daDiChuyenXeTrang
        daDiChuyenXeTrang[0] = true;
        daDiChuyenXeTrang[1] = true;

        daDiChuyenVuaDen = true;
        // Reset mảng boolean daDiChuyenXeDen
        daDiChuyenXeDen[0] = true;
        daDiChuyenXeDen[1] = true;
        // Đặt vua
        banCo[0][0] = new QuanCo(LoaiQuan.VUA, false); // Vua đen
        banCo[7][7] = new QuanCo(LoaiQuan.VUA, true);  // Vua trắng

        // Đặt mã
        banCo[1][2] = new QuanCo(LoaiQuan.MA, false);  // Mã đen
        banCo[6][5] = new QuanCo(LoaiQuan.MA, true);   // Mã trắng

        // Đặt tượng
        banCo[1][5] = new QuanCo(LoaiQuan.TUONG, false); // Tượng đen
        banCo[6][2] = new QuanCo(LoaiQuan.TUONG, true);  // Tượng trắng

        // Đặt xe trắng
        banCo[6][1] = new QuanCo(LoaiQuan.XE, true); // Xe trắng
    }

    private void capNhatHienThi() {
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                ImageView oVuong = cacOVuong[hang][cot];
                QuanCo quan = banCo[hang][cot];


                oVuong.setImageDrawable(null);

                if (quan != null) {
                    try {
                        // Lấy tên file PNG
                        String tenFile = quan.layTenFile();

                        // Lấy resource ID từ tên file
                        int resourceId = getResources().getIdentifier(tenFile, "drawable", getPackageName());

                        if (resourceId != 0) {
                            oVuong.setImageResource(resourceId);
                            oVuong.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } else {
                            hienThiKyHieu(oVuong, quan);
                        }
                    } catch (Exception e) {
                        hienThiKyHieu(oVuong, quan);
                    }
                }
            }
        }
    }
    private void hienThiKyHieu(ImageView oVuong, QuanCo quan) {
        TextView textView = new TextView(this);
        textView.setText(quan.layKyHieu());
        textView.setTextSize(32);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);

        textView.setDrawingCacheEnabled(true);
        textView.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        );
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        textView.buildDrawingCache(true);
        oVuong.setImageBitmap(textView.getDrawingCache());
    }
    private void xuLyNhanVaoO(int hang, int cot) {
        if (gameKetThuc) {
            Toast.makeText(this, "Game đã kết thúc!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oVuongDaChon == null) {
            // Chọn quân cờ
            if (banCo[hang][cot] != null && banCo[hang][cot].laTrang == luotTrang) {
                chonQuan(hang, cot);
            }
        } else {
            // Thực hiện nước đi hoặc chọn quân mới
            if (hang == hangDaChon && cot == cotDaChon) {
                // Bỏ chọn quân hiện tại
                boChonQuan();
            } else if (laNuocDiHopLe(hang, cot)) {
                // Thực hiện nước đi
                lichSuBuocDi.push(new BuocDi(hangDaChon, cotDaChon, hang, cot, banCo[hangDaChon][cotDaChon], banCo[hang][cot], luotTrang));
                thucHienNuocDi(hangDaChon, cotDaChon, hang, cot);
                boChonQuan();
                chuyenLuot();
                capNhatTieuDe();

                // Kiểm tra trạng thái game sau nước đi
                kiemTraTrangThaiGame();
            } else {
                // Thử chọn quân mới
                boChonQuan();
                if (banCo[hang][cot] != null && banCo[hang][cot].laTrang == luotTrang) {
                    chonQuan(hang, cot);
                }
            }
        }
    }
    private void chonQuan(int hang, int cot) {
        oVuongDaChon = cacOVuong[hang][cot];
        hangDaChon = hang;
        cotDaChon = cot;

        // Đổi màu ô được chọn
        oVuongDaChon.setBackgroundColor(Color.YELLOW);

        // Tính toán và hiển thị các nước đi hợp lệ (bao gồm kiểm tra chiếu tướng)
        cacNuocDiHopLe = tinhCacNuocDiHopLeSauChieu(hang, cot);
        hienThiCacNuocDiHopLe();

        if (cacNuocDiHopLe.isEmpty()) {
            Toast.makeText(this, "Không có nước đi hợp lệ cho " + layTenQuan(banCo[hang][cot]), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã chọn " + layTenQuan(banCo[hang][cot]), Toast.LENGTH_SHORT).show();
        }
    }
    private void boChonQuan() {
        if (oVuongDaChon != null) {
            // Khôi phục màu gốc
            int mauGoc = (hangDaChon + cotDaChon) % 2 == 0 ? mauOSang : mauOToi;
            oVuongDaChon.setBackgroundColor(mauGoc);
            oVuongDaChon = null;
            hangDaChon = -1;
            cotDaChon = -1;
        }
        anCacNuocDiHopLe();
    }
    private void hienThiCacNuocDiHopLe() {
        for (int[] nuocDi : cacNuocDiHopLe) {
            cacOVuong[nuocDi[0]][nuocDi[1]].setBackgroundColor(Color.GREEN);
        }
    }
    private void anCacNuocDiHopLe() {
        for (int[] nuocDi : cacNuocDiHopLe) {
            int mauGoc = (nuocDi[0] + nuocDi[1]) % 2 == 0 ? mauOSang : mauOToi;
            cacOVuong[nuocDi[0]][nuocDi[1]].setBackgroundColor(mauGoc);
        }
        cacNuocDiHopLe.clear();
    }
    private boolean laNuocDiHopLe(int hang, int cot) {
        for (int[] nuocDi : cacNuocDiHopLe) {
            if (nuocDi[0] == hang && nuocDi[1] == cot) {
                return true;
            }
        }
        return false;
    }
    private List<int[]> tinhCacNuocDiHopLeSauChieu(int hang, int cot) {
        List<int[]> cacNuocDiCoBan = tinhCacNuocDiHopLe(hang, cot);
        List<int[]> cacNuocDiHopLe = new ArrayList<>();

        for (int[] nuocDi : cacNuocDiCoBan) {
            if (coTheThucHienNuocDi(hang, cot, nuocDi[0], nuocDi[1])) {
                cacNuocDiHopLe.add(nuocDi);
            }
        }

        return cacNuocDiHopLe;
    }
    private boolean coTheThucHienNuocDi(int tuHang, int tuCot, int denHang, int denCot) {
        // Lưu trạng thái hiện tại
        QuanCo quanGoc = banCo[tuHang][tuCot];
        QuanCo quanDich = banCo[denHang][denCot];

        // Thực hiện nước đi tạm thời
        banCo[denHang][denCot] = quanGoc;
        banCo[tuHang][tuCot] = null;

        // Kiểm tra xem vua có bị chiếu không
        boolean vuaBiChieu = kiemTraChieuTuong(quanGoc.laTrang);

        // Khôi phục trạng thái
        banCo[tuHang][tuCot] = quanGoc;
        banCo[denHang][denCot] = quanDich;

        return !vuaBiChieu;
    }
    private List<int[]> tinhCacNuocDiHopLe(int hang, int cot) {
        List<int[]> cacNuocDi = new ArrayList<>();
        QuanCo quan = banCo[hang][cot];

        if (quan == null) return cacNuocDi;

        switch (quan.loai) {
            case TOT:
                themNuocDiTot(cacNuocDi, hang, cot, quan.laTrang);
                break;
            case XE:
                themNuocDiXe(cacNuocDi, hang, cot, quan.laTrang);
                break;
            case MA:
                themNuocDiMa(cacNuocDi, hang, cot, quan.laTrang);
                break;
            case TUONG:
                themNuocDiTuong(cacNuocDi, hang, cot, quan.laTrang);
                break;
            case HAU:
                themNuocDiHau(cacNuocDi, hang, cot, quan.laTrang);
                break;
            case VUA:
                themNuocDiVua(cacNuocDi, hang, cot, quan.laTrang);
                themNhapThanh(cacNuocDi, hang, cot, quan.laTrang);
                break;
        }

        return cacNuocDi;
    }
    private void themNuocDiTot(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        int huong = laTrang ? -1 : 1; // Trắng đi lên (-1), đen đi xuống (+1)
        int hangBatDau = laTrang ? 6 : 1;
        int hangPhongCap = laTrang ? 0 : 7;

        // Đi thẳng 1 ô
        if (laViTriHopLe(hang + huong, cot) && banCo[hang + huong][cot] == null) {
                cacNuocDi.add(new int[]{hang + huong, cot});

            // Đi thẳng 2 ô từ vị trí ban đầu
            if (hang == hangBatDau && banCo[hang + 2 * huong][cot] == null) {
                cacNuocDi.add(new int[]{hang + 2 * huong, cot});
            }
        }

        // Ăn chéo
        for (int doLechCot : new int[]{-1, 1}) {
            int hangMoi = hang + huong;
            int cotMoi = cot + doLechCot;
            if (laViTriHopLe(hangMoi, cotMoi) && banCo[hangMoi][cotMoi] != null
                    && banCo[hangMoi][cotMoi].laTrang != laTrang) {
                cacNuocDi.add(new int[]{hangMoi, cotMoi});
            }
        }
    }
    private void hienThiDialogChonPhongQuan(int hang, int cot, boolean laTrang) {
        // Các quân phong cấp: Hậu, Xe, Mã, Tượng
        final LoaiQuan[] luaChonQuan = {LoaiQuan.HAU, LoaiQuan.XE, LoaiQuan.MA, LoaiQuan.TUONG};

        List<Map<String, Object>> items = new ArrayList<>();
        for (LoaiQuan loai : luaChonQuan) {
            Map<String, Object> item = new HashMap<>();
            QuanCo tempQuan = new QuanCo(loai, laTrang);
            String tenFile = tempQuan.layTenFile();
            int imgResId = getResources().getIdentifier(tenFile, "drawable", getPackageName());
            item.put("img", imgResId);
            item.put("text", loai.name());
            items.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                items,
                R.layout.item_dialog_phong_quan,
                new String[]{"img", "text"},
                new int[]{R.id.imageViewQuan, R.id.textViewQuan}
        );

        new AlertDialog.Builder(this)
                .setTitle("Chọn quân để phong")
                .setAdapter(adapter, (dialog, which) -> {
                    LoaiQuan loaiPhong = luaChonQuan[which];
                    banCo[hang][cot] = new QuanCo(loaiPhong, laTrang);
                    capNhatHienThi();
                })
                .setCancelable(false)
                .show();
    }
    private void themNuocDiXe(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        // Di chuyển theo hàng ngang và hàng dọc
        int[][] cacHuong = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] huong : cacHuong) {
            for (int i = 1; i < 8; i++) {
                int hangMoi = hang + huong[0] * i;
                int cotMoi = cot + huong[1] * i;

                if (!laViTriHopLe(hangMoi, cotMoi)) break;

                if (banCo[hangMoi][cotMoi] == null) {
                    cacNuocDi.add(new int[]{hangMoi, cotMoi});
                } else {
                    if (banCo[hangMoi][cotMoi].laTrang != laTrang) {
                        cacNuocDi.add(new int[]{hangMoi, cotMoi});
                    }
                    break;
                }
            }
        }
    }
    private void themNuocDiMa(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        int[][] cacNuocDiMa = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};

        for (int[] nuocDi : cacNuocDiMa) {
            int hangMoi = hang + nuocDi[0];
            int cotMoi = cot + nuocDi[1];

            if (laViTriHopLe(hangMoi, cotMoi)) {
                if (banCo[hangMoi][cotMoi] == null || banCo[hangMoi][cotMoi].laTrang != laTrang) {
                    cacNuocDi.add(new int[]{hangMoi, cotMoi});
                }
            }
        }
    }
    private void themNuocDiTuong(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        // Di chuyển theo đường chéo
        int[][] cacHuong = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] huong : cacHuong) {
            for (int i = 1; i < 8; i++) {
                int hangMoi = hang + huong[0] * i;
                int cotMoi = cot + huong[1] * i;

                if (!laViTriHopLe(hangMoi, cotMoi)) break;

                if (banCo[hangMoi][cotMoi] == null) {
                    cacNuocDi.add(new int[]{hangMoi, cotMoi});
                } else {
                    if (banCo[hangMoi][cotMoi].laTrang != laTrang) {
                        cacNuocDi.add(new int[]{hangMoi, cotMoi});
                    }
                    break;
                }
            }
        }
    }
    private void themNuocDiHau(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        // Hậu = Xe + Tượng
        themNuocDiXe(cacNuocDi, hang, cot, laTrang);
        themNuocDiTuong(cacNuocDi, hang, cot, laTrang);
    }
    private void themNuocDiVua(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        int[][] cacNuocDiVua = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] nuocDi : cacNuocDiVua) {
            int hangMoi = hang + nuocDi[0];
            int cotMoi = cot + nuocDi[1];

            if (laViTriHopLe(hangMoi, cotMoi)) {
                if (banCo[hangMoi][cotMoi] == null || banCo[hangMoi][cotMoi].laTrang != laTrang) {
                    cacNuocDi.add(new int[]{hangMoi, cotMoi});
                }
            }
        }
    }
    private void themNhapThanh(List<int[]> cacNuocDi, int hang, int cot, boolean laTrang) {
        if (laTrang) {
            // Nhập thành bên trắng (vua tại e1 => [7][4])
            if (!daDiChuyenVuaTrang) {
                // Nhập thành bên vua (O-O) - sang g1 => [7][6]
                if (!daDiChuyenXeTrang[1] &&
                        banCo[7][5] == null && banCo[7][6] == null &&
                        !biChieu(7, 4, true) &&
                        !biChieu(7, 5, true) &&
                        !biChieu(7, 6, true)) {
                    cacNuocDi.add(new int[]{7, 6});
                }

                // Nhập thành bên hậu (O-O-O) - sang c1 => [7][2]
                if (!daDiChuyenXeTrang[0] &&
                        banCo[7][1] == null && banCo[7][2] == null && banCo[7][3] == null &&
                        !biChieu(7, 4, true) &&
                        !biChieu(7, 3, true) &&
                        !biChieu(7, 2, true)) {
                    cacNuocDi.add(new int[]{7, 2});
                }
            }
        } else {
            // Nhập thành bên đen (vua tại e8 => [0][4])
            if (!daDiChuyenVuaDen) {
                if (!daDiChuyenXeDen[1] &&
                        banCo[0][5] == null && banCo[0][6] == null &&
                        !biChieu(0, 4, false) &&
                        !biChieu(0, 5, false) &&
                        !biChieu(0, 6, false)) {
                    cacNuocDi.add(new int[]{0, 6});
                }

                if (!daDiChuyenXeDen[0] &&
                        banCo[0][1] == null && banCo[0][2] == null && banCo[0][3] == null &&
                        !biChieu(0, 4, false) &&
                        !biChieu(0, 3, false) &&
                        !biChieu(0, 2, false)) {
                    cacNuocDi.add(new int[]{0, 2});
                }
            }
        }
    }
    private boolean biChieu(int hang, int cot, boolean laTrang) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                QuanCo quan = banCo[i][j];
                if (quan != null && quan.laTrang != laTrang) {
                    List<int[]> nuocDi = tinhCacNuocDiHopLe(i, j);
                    for (int[] nuoc : nuocDi) {
                        if (nuoc[0] == hang && nuoc[1] == cot) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean laViTriHopLe(int hang, int cot) {
        return hang >= 0 && hang < 8 && cot >= 0 && cot < 8;
    }
    private void undoNuocDi() {
        if (lichSuBuocDi.isEmpty()) {
            Toast.makeText(this, "Không có nước nào để quay lại!", Toast.LENGTH_SHORT).show();
            return;
        }
        boChonQuan();
        BuocDi buocCuoi = lichSuBuocDi.pop();

        // Trả lại vị trí cũ
        banCo[buocCuoi.tuHang][buocCuoi.tuCot] = buocCuoi.quanDiChuyen;
        banCo[buocCuoi.denHang][buocCuoi.denCot] = buocCuoi.quanBiAn;

        // Khôi phục trạng thái daDiChuyen cũ
        buocCuoi.quanDiChuyen.daDiChuyen = buocCuoi.daDiChuyenCu;

        // Khôi phục lượt chơi
        luotTrang  = buocCuoi.luotTrang;
        capNhatTieuDe();
        capNhatHienThi();
    }
    private void anUndo() {
        Button btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setVisibility(View.GONE);
    }

    private void thucHienNuocDi(int tuHang, int tuCot, int denHang, int denCot) {
        QuanCo quan = banCo[tuHang][tuCot];
        QuanCo quanBiAn = banCo[denHang][denCot];

        // Thông báo nước đi
        String thongBao = layTenQuan(quan) + " từ " + layTenViTri(tuHang, tuCot) + " đến " + layTenViTri(denHang, denCot);
        if (quanBiAn != null) {
            thongBao += " (ăn " + layTenQuan(quanBiAn) + ")";
        }
        Toast.makeText(this, thongBao, Toast.LENGTH_SHORT).show();

        // --- XỬ LÝ NHẬP THÀNH ---
        if (quan.loai == LoaiQuan.VUA) {
            // Vua đi 2 ô ngang = nhập thành
            if (tuHang == denHang && Math.abs(denCot - tuCot) == 2) {
                if (quan.laTrang) {
                    // Vua trắng nhập thành
                    if (denCot == 6) {
                        // O-O trắng
                        banCo[7][5] = banCo[7][7];
                        banCo[7][7] = null;
                        daDiChuyenXeTrang[1] = true; // Xe h trắng đã đi
                    } else if (denCot == 2) {
                        // O-O-O trắng
                        banCo[7][3] = banCo[7][0];
                        banCo[7][0] = null;
                        daDiChuyenXeTrang[0] = true; // Xe a trắng đã đi
                    }
                    daDiChuyenVuaTrang = true;
                } else {
                    // Vua đen nhập thành
                    if (denCot == 6) {
                        // O-O đen
                        banCo[0][5] = banCo[0][7];
                        banCo[0][7] = null;
                        daDiChuyenXeDen[1] = true; // Xe h đen đã đi
                    } else if (denCot == 2) {
                        // O-O-O đen
                        banCo[0][3] = banCo[0][0];
                        banCo[0][0] = null;
                        daDiChuyenXeDen[0] = true; // Xe a đen đã đi
                    }
                    daDiChuyenVuaDen = true;
                }
                // Cập nhật vị trí vua sau nhập thành
                banCo[denHang][denCot] = quan;
                banCo[tuHang][tuCot] = null;
                quan.daDiChuyen = true;

                capNhatHienThi();
                return; // Kết thúc luôn vì đã xử lý nhập thành
            }
        }

        // Thực hiện nước đi
        banCo[denHang][denCot] = quan;
        banCo[tuHang][tuCot] = null;
        quan.daDiChuyen = true;

        // Nếu là tốt và tới hàng phong cấp
        if (quan.loai == LoaiQuan.TOT) {
            int hangPhongCap = quan.laTrang ? 0 : 7;
            if (denHang == hangPhongCap) {
                // Gọi dialog chọn phong quân
                hienThiDialogChonPhongQuan(denHang, denCot, quan.laTrang);
                return; // Tạm dừng, đợi người chơi chọn quân phong
            }
        }

        capNhatHienThi();
    }
    private void chuyenLuot() {
        luotTrang = !luotTrang;
    }
    private void capNhatTieuDe() {
        String trangThai = "";
        if (gameKetThuc) {
            trangThai = " - KẾT THÚC";
        } else if (dangBiChieu) {
            trangThai = " - CHIẾU TƯỚNG!";
        }
        setTitle("Chơi 2 người - Lượt: " + (luotTrang ? "Trắng" : "Đen") + trangThai);
    }
    private void kiemTraTrangThaiGame() {
        dangBiChieu = kiemTraChieuTuong(luotTrang);

        if (dangBiChieu) {
            if (kiemTraChieuHet(luotTrang)) {
                // Chiếu hết - game kết thúc
                gameKetThuc = true;
                String nguoiThang = luotTrang ? "Đen" : "Trắng";

                hienThiThanhKetThucGame(nguoiThang + " thắng bằng chiếu hết!");
                setTitle(nguoiThang + " thắng bằng chiếu hết!");

                voHieuHoaBanCo();
                anUndo();
            } else {
                Toast.makeText(this, "CHIẾU TƯỚNG! " + (luotTrang ? "Trắng" : "Đen") + " phải thoát khỏi chiếu!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (kiemTraHoaCo(luotTrang)) {
                gameKetThuc = true;
                hienThiThanhKetThucGame("HÒA CỜ! Không còn nước đi.");
                setTitle("HÒA CỜ! Không còn nước đi.");

                voHieuHoaBanCo();
                anUndo();
            }
        }
    }


    private void hienThiThanhKetThucGame(String thongBao) {
        thanhKetThucGame.setVisibility(View.VISIBLE);
        Toast.makeText(this, thongBao, Toast.LENGTH_LONG).show(); // hoặc bạn có thể hiển thị bằng TextView nếu muốn
    }
    private String layTenQuan(QuanCo quan) {
        if (quan == null) return "";
        String mau = quan.laTrang ? "Trắng" : "Đen";
        String tenQuan = "";

        switch (quan.loai) {
            case TOT: tenQuan = "Tốt"; break;
            case XE: tenQuan = "Xe"; break;
            case MA: tenQuan = "Mã"; break;
            case TUONG: tenQuan = "Tượng"; break;
            case HAU: tenQuan = "Hậu"; break;
            case VUA: tenQuan = "Vua"; break;
        }
        return tenQuan + " " + mau;
    }
    private String layTenViTri(int hang, int cot) {
        char chuCai = (char)('a' + cot);
        int so = 8 - hang;
        return "" + chuCai + so;
    }
    private boolean kiemTraChieuTuong(boolean kiemTraTrang) {
        // Tìm vị trí vua
        int hangVua = -1, cotVua = -1;
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (banCo[hang][cot] != null && banCo[hang][cot].loai == LoaiQuan.VUA
                        && banCo[hang][cot].laTrang == kiemTraTrang) {
                    hangVua = hang;
                    cotVua = cot;
                    break;
                }
            }
        }

        if (hangVua == -1) return false;

        // Kiểm tra xem có quân địch nào có thể tấn công vua không
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (banCo[hang][cot] != null && banCo[hang][cot].laTrang != kiemTraTrang) {
                    List<int[]> cacNuocDi = tinhCacNuocDiHopLe(hang, cot);
                    for (int[] nuocDi : cacNuocDi) {
                        if (nuocDi[0] == hangVua && nuocDi[1] == cotVua) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    private boolean kiemTraChieuHet(boolean kiemTraTrang) {

        if (!kiemTraChieuTuong(kiemTraTrang)) {
            return false;
        }
        // Kiểm tra tất cả quân cờ của người chơi hiện tại
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (banCo[hang][cot] != null && banCo[hang][cot].laTrang == kiemTraTrang) {
                    // Lấy tất cả nước đi hợp lệ của quân này
                    List<int[]> cacNuocDi = tinhCacNuocDiHopLeSauChieu(hang, cot);

                    // Nếu có ít nhất một nước đi hợp lệ, chưa bị chiếu hết
                    if (!cacNuocDi.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private void voHieuHoaBanCo() {
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (cacOVuong[hang][cot] != null) {
                    cacOVuong[hang][cot].setEnabled(false);
                }
            }
        }
    }
    private void kichHoatBanCo() {
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (cacOVuong[hang][cot] != null) {
                    cacOVuong[hang][cot].setEnabled(true);
                }
            }
        }
    }
    private boolean oMauTrang(int hang, int cot) {
        return (hang + cot) % 2 == 0;
    }

    private boolean kiemTraHoaCo(boolean kiemTraTrang) {
        if (kiemTraChieuTuong(kiemTraTrang)) {
            return false;
        }
        if (khongDuLucChieuHet()) {
            return true;
        }
        // Kiểm tra tất cả quân cờ của người chơi hiện tại
        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                if (banCo[hang][cot] != null && banCo[hang][cot].laTrang == kiemTraTrang) {
                    // Lấy tất cả nước đi hợp lệ của quân này
                    List<int[]> cacNuocDi = tinhCacNuocDiHopLeSauChieu(hang, cot);

                    // Nếu có ít nhất một nước đi hợp lệ, chưa hòa
                    if (!cacNuocDi.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    private boolean laHoaTuongCungMau(List<QuanCo> trang, List<QuanCo> den) {
        if (trang.size() == 2 && den.size() == 2) {
            int[] posTuongTrang = null;
            int[] posTuongDen = null;

            // Tìm vị trí tượng trắng
            for (int hang = 0; hang < 8; hang++) {
                for (int cot = 0; cot < 8; cot++) {
                    QuanCo q = banCo[hang][cot];
                    if (q != null && q.loai == LoaiQuan.TUONG) {
                        if (q.laTrang && posTuongTrang == null) {
                            posTuongTrang = new int[]{hang, cot};
                        } else if (!q.laTrang && posTuongDen == null) {
                            posTuongDen = new int[]{hang, cot};
                        }
                    }
                }
            }

            if (posTuongTrang != null && posTuongDen != null) {
                return oMauTrang(posTuongTrang[0], posTuongTrang[1]) == oMauTrang(posTuongDen[0], posTuongDen[1]);
            }
        }
        return false;
    }

    private boolean khongDuLucChieuHet() {
        List<QuanCo> trang = new ArrayList<>();
        List<QuanCo> den = new ArrayList<>();

        for (int hang = 0; hang < 8; hang++) {
            for (int cot = 0; cot < 8; cot++) {
                QuanCo quan = banCo[hang][cot];
                if (quan != null) {
                    if (quan.laTrang) {
                        trang.add(quan);
                    } else {
                        den.add(quan);
                    }
                }
            }
        }

        //Toast.makeText(this, "Quân trắng: " + trang.size() + ", Quân đen: " + den.size(), Toast.LENGTH_SHORT).show();

        boolean ketQua = (
                chiConVuaHoacVuaVa1MaHoacTuong(trang) && chiConVuaHoacVuaVa1MaHoacTuong(den)
        ) || laHoaTuongCungMau(trang, den);

        //Toast.makeText(this, ">>> Kết quả khongDuLucChieuHet: " + ketQua, Toast.LENGTH_SHORT).show();

        return ketQua;
    }

    private boolean chiConVuaHoacVuaVa1MaHoacTuong(List<QuanCo> danhSach) {
        if (danhSach.size() == 1) {
            return danhSach.get(0).loai == LoaiQuan.VUA;
        } else if (danhSach.size() == 2) {
            boolean coVua = false;
            boolean coMaHoacTuong = false;
            for (QuanCo q : danhSach) {
                if (q.loai == LoaiQuan.VUA) coVua = true;
                if (q.loai == LoaiQuan.MA || q.loai == LoaiQuan.TUONG) coMaHoacTuong = true;
            }
            return coVua && coMaHoacTuong;
        }
        return false;
    }
    private void khoiTaoLaiGame() {
        thanhKetThucGame.setVisibility(View.GONE);
        kichHoatBanCo();
        Button btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(v -> undoNuocDi());

        daDiChuyenVuaTrang = false;
        // Reset mảng boolean daDiChuyenXeTrang
        daDiChuyenXeTrang[0] = false;
        daDiChuyenXeTrang[1] = false;

        daDiChuyenVuaDen = false;
        // Reset mảng boolean daDiChuyenXeDen
        daDiChuyenXeDen[0] = false;
        daDiChuyenXeDen[1] = false;

        gameKetThuc = false;
        dangBiChieu = false;
        luotTrang = true;
        oVuongDaChon = null;
        hangDaChon = -1;
        cotDaChon = -1;
        cacNuocDiHopLe.clear();

        setTitle("Chơi 2 người - Lượt: Trắng");
        datQuanCoBanDau();
        capNhatHienThi();
        kiemTraTrangThaiGame();

        Toast.makeText(this, "Bắt đầu game mới!", Toast.LENGTH_SHORT).show();
    }

}