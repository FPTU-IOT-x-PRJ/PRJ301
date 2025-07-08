# quiz-generator.py
import os
import sys
import json
import time
import random
import google.generativeai as genai
from google.api_core.exceptions import ResourceExhausted
import io

if sys.stdout.encoding != 'utf-8':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
if sys.stderr.encoding != 'utf-8':
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

try:
    import fitz  # PyMuPDF
except ImportError:
    # Trả lỗi dưới dạng JSON để Java có thể đọc được
    print(json.dumps({"error": "Thư viện PyMuPDF chưa được cài đặt. Chạy 'pip install PyMuPDF' trên server."}), file=sys.stderr)
    sys.exit(1)

# --- CẤU HÌNH ---
# <<< THAY ĐỔI QUAN TRỌNG: API KEY NÊN LẤY TỪ BIẾN MÔI TRƯỜNG ĐỂ BẢO MẬT >>>
API_KEY = os.getenv("GEMINI_API_KEY", "AIzaSyBBowrQYTN1iZtjKwwRHElE1dQ_z4q0tDw") 
# --- KẾT THÚC CẤU HÌNH ---

if not API_KEY or "YOUR_API_KEY" in API_KEY:
    print(json.dumps({"error": "Vui lòng cung cấp Gemini API Key."}), file=sys.stderr)
    sys.exit(1)

try:
    genai.configure(api_key=API_KEY)
    # Sử dụng model flash để tiết kiệm chi phí và tốc độ
    model = genai.GenerativeModel('gemini-2.5-flash') 
except Exception as e:
    print(json.dumps({"error": f"Lỗi khi cấu hình Gemini: {e}"}), file=sys.stderr)
    sys.exit(1)

def read_document_content(file_path: str) -> str:
    """Đọc nội dung từ một file, hỗ trợ PDF và text."""
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"File không tồn tại: {file_path}")
    file_extension = os.path.splitext(file_path)[1].lower()
    if file_extension == '.pdf':
        try:
            with fitz.open(file_path) as doc:
                return "\n".join(page.get_text() for page in doc)
        except Exception as e:
            raise IOError(f"Lỗi khi đọc file PDF: {e}")
    else:
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                return f.read()
        except Exception as e:
            raise IOError(f"Lỗi khi đọc file văn bản: {e}")


def generate_quiz_from_text(document_content: str) -> str:
    """Gửi nội dung đến Gemini và yêu cầu tạo quiz theo định dạng JSON."""
    prompt = f"""Bạn là một chuyên gia tạo đề kiểm tra. Dựa vào nội dung tài liệu được cung cấp, hãy tạo một bài kiểm tra trắc nghiệm HOÀN CHỈNH.

    **YÊU CẦU:**
    - Quiz phải có một `title` (tiêu đề) bằng tiếng Việt, súc tích và phù hợp với nội dung tài liệu.
    - Quiz phải có một `description` (mô tả) ngắn gọn bằng tiếng Việt.
    - Tạo ra chính xác 5 câu hỏi (`questions`).

    **QUY TẮC CHO MỖI CÂU HỎI:**
    - `questionText` (nội dung câu hỏi) phải rõ ràng và liên quan trực tiếp đến tài liệu.
    - `questionType` phải luôn là chuỗi `"MULTIPLE_CHOICE"`.
    - `options` phải là một mảng chứa **CHÍNH XÁC 4** lựa chọn.
    - Trong 4 lựa chọn, phải có **DUY NHẤT MỘT** lựa chọn đúng (`"isCorrect": true`). Ba lựa chọn còn lại phải sai (`"isCorrect": false`).

    **ĐỊNH DẠNG ĐẦU RA:**
    - Chỉ trả về một đối tượng JSON duy nhất và hợp lệ.
    - **TUYỆT ĐỐI KHÔNG** bao gồm bất kỳ văn bản giải thích, lời chào, hay ký tự markdown nào khác như ````json` hoặc ````.

    **VÍ DỤ VỀ ĐỊNH DẠNG JSON MONG MUỐN:**
    ```json
    {{
    "title": "Tiêu đề Quiz",
    "description": "Mô tả ngắn gọn về nội dung của bài quiz này.",
    "questions": [

        {{
        "questionText": "Câu hỏi số 2?",
        "questionType": "MULTIPLE_CHOICE",
        "options": [
            {{"optionText": "Lựa chọn A", "isCorrect": false}},
            {{"optionText": "Lựa chọn B", "isCorrect": false}},
            {{"optionText": "Lựa chọn C", "isCorrect": true}},
            {{"optionText": "Lựa chọn D", "isCorrect": false}}
        ]
        }}
    ]
    }}
    --- NỘI DUNG TÀI LIỆU ---
    {document_content}
    """
    try:
        response = model.generate_content(prompt)
        text_response = response.text.replace("```json", "").replace("```", "").strip()
        # Đảm bảo nó là JSON hợp lệ trước khi trả về
        json.loads(text_response) 
        return text_response
    except json.JSONDecodeError:
        raise ValueError("Phản hồi từ AI không phải là JSON hợp lệ.")
    except Exception as e:
        raise ConnectionError(f"Lỗi khi gọi Gemini API: {e}")

def main():
    """
    Hàm chính nhận tên file INPUT và OUTPUT từ tham số.
    Đọc từ file input, tạo quiz và GHI KẾT QUẢ vào file output.
    """
    # Kiểm tra đủ 3 tham số: [0]tên script, [1]input, [2]output
    if len(sys.argv) < 3:
        sys.stderr.write(json.dumps({"error": "Thiếu tên file input hoặc output làm tham số."}))
        sys.exit(1)

    # <<< SỬ DỤNG TÊN BIẾN MỚI, KHÔNG DÙNG 'file_path' NỮA >>>
    input_file_path = sys.argv[1]
    output_file_path = sys.argv[2]

    try:
        # 1. Đọc nội dung file input
        document_content = read_document_content(input_file_path) # <<< Sử dụng input_file_path
        if not document_content or not document_content.strip():
            raise ValueError("Nội dung tài liệu rỗng hoặc đọc file thất bại.")

        # 2. Tạo quiz từ nội dung
        quiz_json_string = generate_quiz_from_text(document_content)
        
        # 3. Ghi kết quả ra file output
        with open(output_file_path, 'w', encoding='utf-8') as f:
            f.write(quiz_json_string)

    except Exception as e:
        # Ghi lỗi ra stderr để Java bắt được
        sys.stderr.write(json.dumps({"error": f"Lỗi trong quá trình xử lý của Python: {type(e).__name__} - {e}"}))
        sys.exit(1)

if __name__ == "__main__":
    main()