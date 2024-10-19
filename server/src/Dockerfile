FROM python:3.11-alpine

RUN apk add curl

WORKDIR /app

RUN adduser -D nonroot && chown nonroot: /app
USER nonroot

RUN pip install --upgrade pip

COPY requirements.txt .
RUN pip install --user --no-cache-dir --upgrade -r requirements.txt

COPY main.py /app/main.py
COPY endpoints/ /app/endpoints/
COPY database/ /app/database/

EXPOSE 8000

ENTRYPOINT ["python", "-m"] 
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
