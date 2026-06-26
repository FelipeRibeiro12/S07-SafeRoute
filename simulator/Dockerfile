FROM python:3.12-slim
WORKDIR /app
RUN pip install --no-cache-dir aiohttp
COPY simulator.py .
CMD ["python3", "simulator.py"]
